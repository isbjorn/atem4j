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

package io.maritimus.atem4j.protocol.udp;

import com.sun.istack.internal.NotNull;
import io.maritimus.atem4j.protocol.Packet;
import io.maritimus.atem4j.protocol.ParseException;
import io.maritimus.atem4j.protocol.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

/**
 * Created by Oleg Akimov on 30/07/15.
 */
public class UdpClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(UdpClient.class);

    public static final int DEFAULT_ATEM_PORT = 9910;

    public static final int MAX_SEND_BUF = 4096;
    public static final int MAX_RECEIVE_BUF = 4096;
    public static final int SO_RECEIVE_BUF = 16000;     // socket option
    public static final int SEND_TIMEOUT = 50;          // timeout between send ops in non-blocking mode
    public static final int RECEIVE_TIMEOUT = 50;

    public final InetSocketAddress atemAddress;
    public final int localPort;
    public final DatagramChannel channel;
    public final DatagramSocket socket;

    public final byte[] inBuf = new byte[MAX_RECEIVE_BUF];
    public final ByteBuffer inBB = ByteBuffer.wrap(inBuf);
    public final ByteBuffer outBB = ByteBuffer.allocateDirect(MAX_SEND_BUF);

    public final IUdpClientListener listener;

    public volatile boolean isStopped = false;


    public static UdpClient create(InetSocketAddress atemAddress,
                                   IUdpClientListener listener) throws java.io.IOException {
        return new UdpClient(atemAddress, 0, listener);
    }

    public static UdpClient create(String atemHost,
                                   IUdpClientListener listener) throws java.io.IOException {
        return create(createAtemAddress(atemHost, DEFAULT_ATEM_PORT), listener);
    }

    public static UdpClient create(String atemHost,
                                   int localPortMin,
                                   int localPortMax,
                                   IUdpClientListener listener) throws java.io.IOException {
        return create(createAtemAddress(atemHost), localPortMin, localPortMax, listener);
    }

    public static UdpClient create(InetSocketAddress atemAddresss,
                                   int localPortMin,
                                   int localPortMax,
                                   IUdpClientListener listener) throws java.io.IOException {
        if (localPortMax < localPortMin) {
            throw new IllegalArgumentException(String.format(
                    "localPortMin = %d must be lesser or equal to localPortMax = %d",
                    localPortMin,
                    localPortMax
            ));
        }

        int localPort = localPortMax == localPortMin ? localPortMin : Utils.random(localPortMin, localPortMax);
        return new UdpClient(atemAddresss, localPort, listener);
    }

    public static InetSocketAddress createAtemAddress(@NotNull String atemHost) {
        return createAtemAddress(atemHost, DEFAULT_ATEM_PORT);
    }

    public static InetSocketAddress createAtemAddress(@NotNull String atemHost, int atemPort) {
        if (atemHost == null) {
            throw new IllegalArgumentException("atemHost must be not null");
        }

        if (atemPort < 0 || atemPort > 65535) {
            throw new IllegalArgumentException(String.format("atemPort = %d must be in [1,65535]", atemPort));
        }

        return new InetSocketAddress(atemHost, atemPort);
    }

    public UdpClient(@NotNull InetSocketAddress atemAddress, int localPort, @NotNull IUdpClientListener listener) throws java.io.IOException {

        if (atemAddress == null) {
            throw new IllegalArgumentException("atemAddress must be not null");
        }

        if (localPort < 0 || localPort > 65535) {
            throw new IllegalArgumentException(String.format("localPort = %d must be in [1,65535]", localPort));
        }

        if (listener == null) {
            throw new IllegalArgumentException("listener must be not null");
        }

        this.atemAddress = atemAddress;
        this.listener = listener;

        SocketAddress localAddress = new InetSocketAddress(localPort);

        log.debug("binding to " + localAddress);
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(localAddress);
        channel.connect(atemAddress);

        socket = channel.socket();
        this.localPort = socket.getLocalPort();
    }

    public void stop() {
        listener.onClientStop();
        isStopped = true;
        if (channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException e) {
                log.error("Can't close udp client channel", e);
            }
        }
    }

    public void loop() {
        int num = 0;
        int bytesRead;
        while(true) {
            if (isStopped) {
                log.debug("udp client is stopped");
                break;
            }

            bytesRead = 0;
            try {
                inBB.clear();
                bytesRead = channel.read(inBB);
                // inBB.position === bytesRead
            } catch (PortUnreachableException ex) {
                listener.onPortUnreachableException(ex);
            } catch (Exception ex) {
                log.error("Error reading from server", ex);
            } finally {
                if (bytesRead > 0) {
                    inBB.flip();
                } else {
                    inBB.clear();
                    inBB.limit(0);
                }
            }

            if (inBB.hasRemaining()) {
                num = num + 1;
                try {
                    Packet packet = Packet.read(inBB);
                    listener.onPacketReceived(packet);
                    log.debug(String.format(
                            "UDP packet #%d received from server: %s",
                            num,
                            packet
                    ));
                } catch (ParseException ex) {
                    listener.onParseException(ex);
                }
            } else {
                try {
                    Thread.sleep(RECEIVE_TIMEOUT);
                } catch (InterruptedException ex) {
                    if (Thread.interrupted()) {
                        stop();
                        break;
                    }
                }
            }

        }
    }

    synchronized
    public int send(@NotNull Packet p) throws IOException {
        if (p == null) {
            throw new IllegalArgumentException("packet p can't be null");
        }

        outBB.clear();
        p.write(outBB);
        outBB.flip();

        if (!outBB.hasRemaining()) {
            throw new IllegalArgumentException(String.format("packet don't have data: ", p));
        }

        int remaining = outBB.remaining();
        int bytesSent = 0;

        while(!isStopped && (bytesSent = channel.send(outBB, atemAddress)) == 0) {
            try {
                Thread.sleep(SEND_TIMEOUT);
            } catch (InterruptedException ex) {
                if (Thread.interrupted()) {
                    stop();
                    return -1;
                }
            }
        }

        if (bytesSent != remaining) {
            log.debug(String.format("Amount of bytes sent = %d is not equal to buffer size = %d", bytesSent, remaining));
        }

        return bytesSent;
    }

    @Override
    public void close() throws Exception {
        stop();
        listener.onClientClose();// TODO: move after channel close
        if (channel != null) {
            if (channel.isOpen()) {
                channel.close();
            }
        }
    }
}