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

import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.testng.Assert.*;


/**
 * Created by Oleg Akimov on 31/07/15.
 */
public class PacketHelloTest {
    @Test
    public void testServerHello() throws Exception {
        ByteBuffer buf = Utils.parseHexString("10 14 22 f1 00 00 00 00 00 18 00 00 02000010 00000000");
        PacketHello p = (PacketHello) Packet.read(buf);

        PacketHeader header = p.header;

        assertTrue(header.isHello(), "is hello");
        assertEquals(header.ackId, 0, "ack id");
        assertEquals(header.packageId, 0, "package id");
        assertEquals(header.size, 12 + 8, "size");
        assertEquals(header.uid, 0x22F1, "uid");
        assertEquals(header.unicorn, 0x18, "unicorn");

        assertTrue(p.commands.isEmpty(), "no commands");

        assertEquals(p.connectionStatus, 0x02, "connection flag");
        assertTrue(p.isConnected(), "is connected");
        assertEquals(p.connectionUptime, 0x10, "connection uptime");
        assertEquals(p.payload, "0200001000000000", "payload");
    }

    @Test
    public void testClientHello() throws Exception {
        ByteBuffer buf = Utils.parseHexString("1014 3522 0000 0000 0068 0000 0100000000000000");
        PacketHello p = (PacketHello) Packet.read(buf);

        PacketHeader header = p.header;

        assertEquals(header.bitmask, header.FLAG_HELLO, "bitmask");
        assertTrue(header.isHello(), "is hello");
        assertFalse(header.isResend(), "is resend");
        assertEquals(header.ackId, 0, "ack id");
        assertEquals(header.packageId, 0, "package id");
        assertEquals(header.size, 12 + 8, "size");
        assertEquals(header.uid, 0x3522, "uid");
        assertEquals(header.unicorn, 0x68, "unicorn");

        assertTrue(p.commands.isEmpty(), "no commands");

        assertEquals(p.connectionStatus, 0x01, "connection flag");
        assertFalse(p.isConnected(), "is connected");
        assertEquals(p.connectionUptime, 0, "connection uptime");
        assertEquals(p.payload, "0100000000000000", "payload");
    }

    @Test
    public void testCreateClientHello() throws Exception {
        PacketHello p = PacketHello.createClientFirstHello(1000);

        PacketHeader header = p.header;

        assertEquals(header.bitmask, header.FLAG_HELLO, "bitmask");
        assertTrue(header.isHello(), "is hello");
        assertFalse(header.isResend(), "is resend");
        assertEquals(header.ackId, 0, "ack id");
        assertEquals(header.packageId, 0, "package id");
        assertEquals(header.size, 12 + 8, "size");
        assertEquals(header.uid, 1000, "uid");
        assertEquals(header.unicorn, 0x68, "unicorn");

        assertTrue(p.commands.isEmpty(), "no commands");

        assertEquals(p.connectionStatus, 0x01, "connection flag");
        assertFalse(p.isConnected(), "is connected");
        assertEquals(p.connectionUptime, 0, "connection uptime");
        assertEquals(p.payload, "0100000000000000", "payload");
    }

    @Test
    public void testClientResendHello() throws Exception {
        ByteBuffer buf = Utils.parseHexString("3014 4d80 0000 0000 00bd 0000 0100000000000000");
        PacketHello p = (PacketHello) Packet.read(buf);

        PacketHeader header = p.header;

        assertEquals(header.bitmask, header.FLAG_HELLO | header.FLAG_RESEND, "bitmask");
        assertTrue(header.isHello(), "is hello");
        assertTrue(header.isResend(), "is resend");
        assertEquals(header.ackId, 0, "ack id");
        assertEquals(header.packageId, 0, "package id");
        assertEquals(header.size, 12 + 8, "size");
        assertEquals(header.uid, 0x4d80, "uid");
        assertEquals(header.unicorn, 0xbd, "unicorn");

        assertTrue(p.commands.isEmpty(), "no commands");

        assertEquals(p.connectionStatus, 0x01, "connection flag");
        assertFalse(p.isConnected(), "is connected");
        assertEquals(p.connectionUptime, 0, "connection uptime");
        assertEquals(p.payload, "0100000000000000", "payload");
    }

    @Test
    public void testPacketHelloWrite() throws Exception {
        ByteBuffer expected = Utils.parseHexString("3014 4d80 0000 0000 00bd 0000 0100000000000000");
        PacketHello p = PacketHello.createClientResendHello(0x4d80);
        ByteBuffer out = ByteBuffer.allocate(12 + 8);
        out.clear();
        p.write(out);
        out.flip();
        assertEquals(expected.array(), out.array());
    }

    @Test
    public void testCreateClientResendHello() throws Exception {
        PacketHello p = PacketHello.createClientResendHello(2000);

        PacketHeader header = p.header;

        assertEquals(header.bitmask, header.FLAG_HELLO | header.FLAG_RESEND, "bitmask");
        assertTrue(header.isHello(), "is hello");
        assertTrue(header.isResend(), "is resend");
        assertEquals(header.ackId, 0, "ack id");
        assertEquals(header.packageId, 0, "package id");
        assertEquals(header.size, 12 + 8, "size");
        assertEquals(header.uid, 2000, "uid");
        assertEquals(header.unicorn, 0xbd, "unicorn");

        assertTrue(p.commands.isEmpty(), "no commands");

        assertEquals(p.connectionStatus, 0x01, "connection flag");
        assertFalse(p.isConnected(), "is connected");
        assertEquals(p.connectionUptime, 0, "connection uptime");
        assertEquals(p.payload, "0100000000000000", "payload");
    }
}
