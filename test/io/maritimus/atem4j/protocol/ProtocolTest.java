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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.xml.bind.DatatypeConverter;

import java.nio.ByteBuffer;

import static org.testng.Assert.*;

/**
 * Created by Oleg Akimov on 23/07/15.
 */
public class ProtocolTest {

    public static ByteBuffer parseHexString(String str) {
        return ByteBuffer.wrap(DatatypeConverter.parseHexBinary(str));
    }

    @Test void testCharToIntConvertion() throws Exception {
        char from = 0xFFFF;
        int to = from;
        assertTrue(to > 0);
        assertEquals(to, 0xFFFF);
    }

    @Test
    public void testReadClientPacket() throws Exception {
        ByteBuffer buf = parseHexString("08188001000000000000002e000c3e74435076490004c0d5");

        PacketHeader header = PacketHeader.read(buf);
        assertEquals(header.bitmask     , PacketHeader.FLAG_ACKREQ, "bitmask");
        assertEquals(header.size        , 0x0018, "blockSize");
        assertEquals(header.uid         , 0x8001, "uid");
        assertEquals(header.ackId       , 0x0   , "ackId");
        assertEquals(header.unicorn     , 0x0   , "unicorn");
        assertEquals(header.packageId   , 0x002e, "packageId");

        CmdUnknown cmd1 = (CmdUnknown)Command.read(buf);
        assertEquals(cmd1.blockSize     , 0x000c, "cmd1 block size");
        assertEquals(cmd1.payloadHex    , "3e74435076490004c0d5".toUpperCase(), "cmd1 payload");
    }

    @Test
    public void testReadServerPacket() throws Exception {
        ByteBuffer buf = parseHexString("882c8001002e00000000002300140000546c496e000801000002000000000000000c00005072764900040000");

        PacketHeader header = PacketHeader.read(buf);
        assertEquals(header.bitmask     , PacketHeader.FLAG_ACKREQ | PacketHeader.FLAG_ACK, "bitmask");
        assertEquals(header.size        , 12 + 20 + 12, "blockSize");
        assertEquals(header.uid         , 0x8001, "uid");
        assertEquals(header.unicorn     , 0x0, "unicorn");

        assertTrue((PacketHeader.FLAG_ACKREQ | header.bitmask) > 0, "this is request for ack from you");
        assertEquals(header.packageId   , 0x0023, "packageId");

        assertTrue((PacketHeader.FLAG_ACK | header.bitmask) > 0, "this is ack for your request");
        assertEquals(header.ackId       , 0x002e, "ackId");

        CmdUnknown cmd1 = (CmdUnknown)Command.read(buf);
        assertEquals(cmd1.blockSize     , 0x0014, "cmd1 block size");
        assertEquals(cmd1.payloadHex    , "0000546c496e000801000002000000000000".toUpperCase(), "cmd1 payload");

        CmdUnknown cmd2 = (CmdUnknown)Command.read(buf);
        assertEquals(cmd2.blockSize     , 0x000C, "cmd2 block size");
        assertEquals(cmd2.payloadHex    , "00005072764900040000".toUpperCase(), "cmd2 payload");

        assertEquals(buf.remaining(), 0, "buf is empty");
    }
}