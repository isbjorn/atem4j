/*
 * Copyright (C) 2015 Oleg Akimov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.maritimus.atem4j.protocol;

import io.maritimus.atem4j.protocol.command.CmdInitializationComplete;
import io.maritimus.atem4j.protocol.command.Command;
import io.maritimus.atem4j.protocol.udp.IUdpClientListener;
import io.maritimus.atem4j.protocol.udp.StateTimeoutException;
import io.maritimus.atem4j.protocol.udp.UdpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Oleg Akimov on 01/08/15.
 */
public class AtemClient implements IUdpClientListener {

    private static final Logger log = LoggerFactory.getLogger(AtemClient.class);
    public static final int RND_UID_MIN = 10000;
    public static final int RND_UID_MAX = 60000;
    public static final long LOOP_SLEEP_MS = 5;

    public static final String STATE_SLEEPING = "SLEEPING";
    public static final String STATE_CONNECTING = "CONNECTING";
    public static final String STATE_INITIALIZING = "INITIALIZING";
    public static final String STATE_WORKING = "WORKING";
    public static final String STATE_RECOVERING = "RECOVERING";

    public static final long TIMEOUT_SLEEPING_MS = 100;
    public static final long TIMEOUT_HELLO_MS = 200;            // 0.2 sec
    public static final long TIMEOUT_CONNECTING_MS = 4000;      // 2 sec by protocol, 4 sec for bad connection
    public static final long TIMEOUT_INITIALIZING_MS = 2000;    // 2 sec???
    public static final long TIMEOUT_WORKING_MS = 1500;         // 1.5 sec
    public static final long TIMEOUT_RECOVERING_MS = 2000;      // 2 sec
    public static final long TIMEOUT_ACKREQ_MS = 200;           // 0.2 sec

    private String state = null;
    private UdpClient client = null;
    private Thread udpThread = null;
    private int packageId = 0;
    private int uid = 0;
    private long lastTime = 0;      // last server answers time
    private long markerTime = 0;    // marker time, ex.: hello timeout
    private BlockingQueue<Packet> receiveQueue = new LinkedBlockingQueue<>();

    private volatile boolean isStopped = false;

    public final InetSocketAddress atemAddress;

    public static AtemClient create(InetSocketAddress atemAddress) {
        AtemClient client = new AtemClient(atemAddress);
        client.toStopped();
        return client;
    }

    protected AtemClient(InetSocketAddress atemAddress) {
        this.atemAddress = atemAddress;
        client = null;
        udpThread = null;
        state = null;
    }

    protected void setState(String newState) {
        log.debug(String.format("changing state from %s to %s", state, newState));
        state = newState;
    }

    protected void toInitializing() {
        setState(STATE_INITIALIZING);

        updateLastTime();

        log.info(String.format(
                "connected to %s with uid %d from port %d",
                atemAddress,
                uid,
                client.localPort
        ));
    }

    protected void toWorking() {
        setState(STATE_WORKING);

        updateLastTime();

        log.info("initialized");
    }

    protected void toRecovering() throws IOException {
        setState(STATE_RECOVERING);

        sendAckRequest();

        updateLastTime();

        log.info("connection lost, recovering");
    }

    protected void toStopped() {
        setState(STATE_SLEEPING);

        packageId = 0;
        uid = Utils.random(RND_UID_MIN, RND_UID_MAX);

        long timeout = TIMEOUT_SLEEPING_MS;

        if (client != null) {
            client.stop();
        }

        receiveQueue.clear();

        setMarkerItme(timeout);
    }

    protected void toConnecting() throws IOException {
        setState(STATE_CONNECTING);

        receiveQueue.clear();

        // create upd client
        try {
            client = UdpClient.create(atemAddress, 0, 0, this);
        } catch(IOException ex) {
            log.error("Can't create udp client", ex);

            try {
               client = UdpClient.create(atemAddress, 0, 0, this);
            } catch (IOException ex2) {
                log.error("Can't create udp client (2nd try), aborting", ex2);
                toStopped();
                throw new RuntimeException("Can't create udp client", ex2);
            }
        }

        // start udp client
        udpThread = new Thread(new UdpProcess(client));
        udpThread.start();

        // send hello message
        sendHello();
        setMarkerItme(TIMEOUT_HELLO_MS);

        // reset last time
        updateLastTime();

        log.info(String.format(
                "connecting to %s with uid %d from port %d",
                atemAddress,
                uid,
                client.localPort
        ));
    }

    public void stop() {
        this.isStopped = true;
    }

    protected void setUid(int newUid) {
        log.info(String.format("changing Uid from %d to %d", uid, newUid));
        uid = newUid;
    }

    public void loop() {
        int j = 10000;
        loop: while(true) {
            if (isStopped) {
                toStopped();
                break;
            }

            try {
                switch (state) {
                    case STATE_SLEEPING:
                        doSleeping();
                        break;

                    case STATE_CONNECTING:
                        doConnecting();
                        break;

                    case STATE_INITIALIZING:
                        doInitializing();
                        break;

                    case STATE_WORKING:
                        doWorking();
                        break;


                    case STATE_RECOVERING:
                        doRecovering();
                        break;

                    default:
                        log.error(String.format("State %s is not supported yet", state));
                        stop();
                        break;
                }

                Thread.sleep(LOOP_SLEEP_MS);
            } catch (IOException ex) {
                log.error("socket error", ex);
                toStopped();
            } catch (StateTimeoutException ex) {
                log.info(ex.getMessage());
                if (state == STATE_WORKING) {
                    try {
                        toRecovering();
                    } catch (IOException ex2) {
                        log.error("socket error", ex2);
                        toStopped();
                    }
                } else {
                    toStopped();
                }
            } catch (InterruptedException e) {
                log.debug("sleep is interrupted", e);
                stop();
            }

            if (--j < 0) {
                log.debug("end of J");
                stop();
            }
        }

        log.debug("loop is done");
    }

    protected void applyCommand(Command cmd) {
        log.trace(String.format("skipping command: %s", cmd.getClass().getSimpleName()));
    }

    protected void sendAck(Packet atemPacket) throws IOException {
        try {
            int atemPackageId = atemPacket.header.packageId;
            PacketHeader header = new PacketHeader(
                    PacketHeader.FLAG_ACK,
                    PacketHeader.HEADER_LENGTH,
                    uid,
                    atemPackageId,
                    0,
                    0
            );
            Packet packet = new Packet(header);
            log.debug(String.format("sending Ack for packageId = %d", atemPackageId));
            client.send(packet);
        } catch (PortUnreachableException ex) {
            log.trace(String.format("atem %s is unreachable", client.atemAddress));
        }
    }

    protected void sendHello() throws IOException {
        try {
            client.send(PacketHello.createClientFirstHello(uid));
            log.debug(String.format("sending Hello to atem %s", client.atemAddress));
        } catch (PortUnreachableException ex) {
            log.trace(String.format("atem %s is unreachable", client.atemAddress));
        }
    }

    protected void resendHello() throws IOException {
        try {
            client.send(PacketHello.createClientResendHello(uid));
            log.debug(String.format("resending Hello to atem %s", client.atemAddress));
        } catch (PortUnreachableException ex) {
            log.trace(String.format("atem %s is unreachable", client.atemAddress));
        }
    }

    public int getNextPackageId() {
        packageId = packageId + 1;
        return packageId;
    }

    public void sendAckRequest() throws IOException {
        try {
            int clientPackageId = getNextPackageId();
            PacketHeader header = new PacketHeader(
                    PacketHeader.FLAG_ACKREQ,
                    PacketHeader.HEADER_LENGTH,
                    uid,
                    0,
                    clientPackageId,
                    0
            );
            Packet packet = new Packet(header);
            client.send(packet);
            log.debug(String.format("sending AckReq for packageId = %d", clientPackageId));
        } catch (PortUnreachableException ex) {
            log.trace(String.format("atem %s is unreachable", client.atemAddress));
        }
    }

    public void resendLastAckRequest() throws IOException {
        try {
            int clientPackageId = packageId;
            PacketHeader header = new PacketHeader(
                    PacketHeader.FLAG_ACKREQ | PacketHeader.FLAG_RESEND,
                    PacketHeader.HEADER_LENGTH,
                    uid,
                    0,
                    clientPackageId,
                    0
            );
            Packet packet = new Packet(header);
            client.send(packet);
            log.debug(String.format("resending AckReq for packageId = %d", clientPackageId));
        } catch (PortUnreachableException ex) {
            log.trace(String.format("atem %s is unreachable", client.atemAddress));
        }
    }

    protected void doWorking() throws IOException, StateTimeoutException {
        if (receiveQueue.isEmpty()) {
            checkLastTimeout(TIMEOUT_WORKING_MS);
            return;
        }

        Packet packet;
        queue:
        while ((packet = receiveQueue.poll()) != null) {

            updateLastTime();

            if (packet.header.isAckRequest()) {
                sendAck(packet);
            }

            if (packet.header.uid != 0 && packet.header.uid != uid) {
                setUid(packet.header.uid);
            }

            for (Command cmd : packet.commands) {
                applyCommand(cmd);
            }
        }

        //todo: make ackreq
    }

    protected void doInitializing() throws IOException, StateTimeoutException {

        if (receiveQueue.isEmpty()) {
            checkLastTimeout(TIMEOUT_INITIALIZING_MS);
            return;
        }

        Packet packet;
        boolean completed = false;

        queue:
        while ((packet = receiveQueue.poll()) != null) {

            updateLastTime();

            //ack only hello until full initialization
            if (packet.header.isHello()) {
                sendAck(packet);
                continue;
            }

            if (packet.header.uid != 0 && packet.header.uid != uid) {
                setUid(packet.header.uid);
            }

            for (Command cmd : packet.commands) {
                if (cmd instanceof CmdInitializationComplete) {
                    completed = true;
                    continue;
                }

                applyCommand(cmd);
            }

            if (completed) {
                break;
            }
        }

        if (completed) {
            log.info("initialization complete");
            toWorking();
        }
    }

    protected void doConnecting() throws IOException, StateTimeoutException {

        if (receiveQueue.isEmpty()) {
            checkLastTimeout(TIMEOUT_CONNECTING_MS);
            return;
        }

        Packet packet;

        queue:
        while ((packet = receiveQueue.poll()) != null) {

            updateLastTime();

            if (!packet.header.isHello()) {
                log.debug(String.format("ignoring packet on connecting: %s", packet));
            }

            sendAck(packet);
            toInitializing();
            return;
        }

        if (isMarkerTime()) {
            resendHello();
            setMarkerItme(TIMEOUT_HELLO_MS);
        }
    }

    protected void doRecovering() throws IOException, StateTimeoutException {

        if (!receiveQueue.isEmpty()) {
            toWorking();
            return;
        }

        checkLastTimeout(TIMEOUT_RECOVERING_MS);

        if (isMarkerTime()) {
            resendLastAckRequest();
            setMarkerItme(TIMEOUT_ACKREQ_MS);
        }
    }

    protected void doSleeping() throws IOException {
        // closing client
        if (client != null) {
            if (client.channel.isOpen()) {
                log.debug("waiting for channel");
                return;
            } else {
                log.debug("channel is closed, clearing client");
                client = null;
                udpThread = null; // todo: wait for thread
            }
        }

        if (isMarkerTime()) {
            toConnecting();
        }
    }

    protected void updateLastTime() {
        lastTime = System.currentTimeMillis();
        log.debug(String.format("update last time to %d", lastTime));
    }

    protected void setMarkerItme(long offset) {
        markerTime = System.currentTimeMillis() + offset;
    }

    protected boolean isMarkerTime() {
        if (markerTime == 0) {
            return false;
        }

        return markerTime < System.currentTimeMillis();
    }

    public boolean isLastTimeout(long timeout) {
        return lastTime == 0 ? false : System.currentTimeMillis() > lastTime + timeout;
    }

    public void checkLastTimeout(long timeout) throws StateTimeoutException {
        if (isLastTimeout(timeout)) {
            log.debug(String.format(
                    "last timeout check is failed, last time = %d, current time = %d, timeout = %d",
                    lastTime,
                    System.currentTimeMillis(),
                    timeout
            ));
            throw new StateTimeoutException("timeout on state %s", state);
        }
    }

    @Override
    public void onPacketReceived(Packet packet) {
        log.trace(String.format("onPacketReceived %s", packet.toString()));
        receiveQueue.add(packet);
    }

    @Override
    public void onPortUnreachableException(PortUnreachableException ex) {
        // do nothing
    }

    @Override
    public void onClientClose() {
        log.trace("onClientClose");
    }

    @Override
    public void onClientStop() {
        log.trace("onClientStop");
    }

    @Override
    public void onParseException(ParseException ex) {
        log.error("onParseException", ex);
    }

    class UdpProcess implements Runnable {

        public final UdpClient client;

        UdpProcess(UdpClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            client.loop();
        }
    }
}
