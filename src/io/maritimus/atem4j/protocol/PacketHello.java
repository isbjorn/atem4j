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

import com.sun.istack.internal.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Oleg Akimov on 30/07/15.
 */
public class PacketHello extends Packet {

    public static final int STATUS_CLIENT = 1;
    public static final int STATUS_CONNECTED = 2;
    public static final int STATUS_FULLY_BOOKED = 3;
    public static final int STATUS_RECONNECT = 4;

    public static final long FIRST_UNICORN = 0x68;
    public static final int FIRST_STATUS = STATUS_CLIENT;
    public static final String FIRST_PAYLOAD = "0100000000000000";

    public static final long RESEND_UNICORN = 0xBD;
    public static final int RESEND_STATUS = STATUS_CLIENT;
    public static final String RESEND_PAYLOAD = "0100000000000000";


    public final int connectionStatus;
    public final int connectionUptime;
    public final String payload;

    public PacketHello(@NotNull PacketHeader header, int connectionStatus, int connectionUptime, String payload) {
        super(header, new ArrayList<>());
        this.connectionStatus = connectionStatus;
        this.connectionUptime = connectionUptime;
        this.payload = payload;
    }

    public boolean isConnected() {
        return (connectionStatus & STATUS_CONNECTED) != 0;
    }

    public static PacketHello createClientFirstHello(int uid) {
        PacketHeader header = new PacketHeader(
                PacketHeader.FLAG_HELLO,
                PacketHeader.HEADER_LENGTH + FIRST_PAYLOAD.length() / 2,
                uid,
                0, 0,
                FIRST_UNICORN
        );

        return new PacketHello(header, FIRST_STATUS, 0, FIRST_PAYLOAD);
    }

    public static PacketHello createClientResendHello(int uid) {
        PacketHeader header = new PacketHeader(
                PacketHeader.FLAG_HELLO | PacketHeader.FLAG_RESEND,
                PacketHeader.HEADER_LENGTH + RESEND_PAYLOAD.length() / 2,
                uid,
                0, 0,
                RESEND_UNICORN
        );

        return new PacketHello(header, RESEND_STATUS, 0, RESEND_PAYLOAD);
    }

    @Override
    public void write(@NotNull ByteBuffer buf) {
        super.write(buf);
        buf.put(Utils.parseHexString(payload));
    }

    @Override
    public String toString() {
        return String.format(
                "%s | status=%s%s%s%s%s uptime=%d",
                super.toString(),
                connectionStatus == STATUS_CLIENT ? "CLT " : "",
                connectionStatus == STATUS_CONNECTED ? "CON" : "",
                connectionStatus == STATUS_FULLY_BOOKED ? "BOOKED" : "",
                connectionStatus == STATUS_RECONNECT ? "RECON" : "",
                connectionStatus > 4 ? String.format("UNKW:%d", connectionStatus) : "",
                connectionUptime
        );
    }
}
