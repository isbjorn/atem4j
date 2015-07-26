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

    private static final Logger log = LoggerFactory.getLogger(ProtocolTest.class);

    public static ByteBuffer parseHexString(String str) {
        return ByteBuffer.wrap(DatatypeConverter.parseHexBinary(str));
    }

    @Test void testStringify() {
        int rawCommand = 0x546c496e;
        assertEquals(Command.stringify(rawCommand), "TlIn");
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
        assertEquals(cmd1.command       , "CPvI");
        assertEquals(cmd1.blockSize, 0x000c, "cmd1 block size");
        assertEquals(cmd1.payloadHex    , "000C3E74435076490004C0D5".toUpperCase(), "cmd1 payload");
    }

    @Test
    public void testReadServerPacket() throws Exception {
        ByteBuffer buf = parseHexString("882c8001002e00000000002300140000546c496e000801000002000000000000000c00005072764900040000");

        PacketHeader header = PacketHeader.read(buf);
        assertEquals(header.bitmask, PacketHeader.FLAG_ACKREQ | PacketHeader.FLAG_ACK, "bitmask");
        assertEquals(header.size, 12 + 20 + 12, "blockSize");
        assertEquals(header.uid, 0x8001, "uid");
        assertEquals(header.unicorn, 0x0, "unicorn");

        assertTrue((PacketHeader.FLAG_ACKREQ | header.bitmask) > 0, "this is request for ack from you");
        assertEquals(header.packageId, 0x0023, "packageId");

        assertTrue((PacketHeader.FLAG_ACK | header.bitmask) > 0, "this is ack for your request");
        assertEquals(header.ackId, 0x002e, "ackId");

        // cmd1
        CmdTallyByIndex cmd1 = (CmdTallyByIndex)Command.read(buf);
        assertEquals(cmd1.flags,
                new byte[]{CmdTallyByIndex.FLAG_PROGRAM, 0, 0, CmdTallyByIndex.FLAG_PREVIEW, 0, 0, 0, 0},
                "flags");

        assertEquals(cmd1.length, 8, "cmd1 length");

        assertTrue(cmd1.isOnProgram(0));
        assertFalse(cmd1.isOnPreview(0));

        assertFalse(cmd1.isOnProgram(1));
        assertFalse(cmd1.isOnPreview(1));
        assertFalse(cmd1.isOnProgram(2));
        assertFalse(cmd1.isOnPreview(2));

        assertFalse(cmd1.isOnProgram(3));
        assertTrue(cmd1.isOnPreview(3));

        assertFalse(cmd1.isOnProgram(4));
        assertFalse(cmd1.isOnPreview(4));
        assertFalse(cmd1.isOnProgram(5));
        assertFalse(cmd1.isOnPreview(5));
        assertFalse(cmd1.isOnProgram(6));
        assertFalse(cmd1.isOnPreview(6));
        assertFalse(cmd1.isOnProgram(7));
        assertFalse(cmd1.isOnPreview(7));

        // cmd2
        CmdUnknown cmd2 = (CmdUnknown)Command.read(buf);
        assertEquals(cmd2.command       , "PrvI");
        assertEquals(cmd2.blockSize     , 0x000C, "cmd2 block size");
        assertEquals(cmd2.payloadHex    , "000C00005072764900040000".toUpperCase(), "cmd2 payload");

        assertEquals(buf.remaining(), 0, "buf is empty");
    }
}