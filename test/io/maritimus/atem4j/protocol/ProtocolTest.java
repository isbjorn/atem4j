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

import java.nio.ByteBuffer;

import static org.testng.Assert.*;

/**
 * Created by Oleg Akimov on 23/07/15.
 */
public class ProtocolTest {

    private static final Logger log = LoggerFactory.getLogger(ProtocolTest.class);

    @Test void testStringify() {
        int rawCommand = 0x546c496e;
        assertEquals(Utils.stringifyCommand(rawCommand), "TlIn");
    }

    @Test void testCharToIntConvertion() throws Exception {
        char from = 0xFFFF;
        int to = from;
        assertTrue(to > 0);
        assertEquals(to, 0xFFFF);
    }

    @Test
    public void testReadClientPacket() throws Exception {
        ByteBuffer buf = Utils.parseHexString("08188001000000000000002e000c3e74435076490004c0d5");

        PacketHeader header = PacketHeader.read(buf);
        assertEquals(header.bitmask     , PacketHeader.FLAG_ACKREQ, "bitmask");
        assertEquals(header.size        , 0x0018, "blockSize");
        assertEquals(header.uid         , 0x8001, "uid");
        assertEquals(header.ackId       , 0x0   , "ackId");
        assertEquals(header.unicorn     , 0x0   , "unicorn");
        assertEquals(header.packageId, 0x002e, "packageId");

        CmdUnknown cmd1 = (CmdUnknown)Command.read(buf);
        assertEquals(cmd1.command       , "CPvI");
        assertEquals(cmd1.blockSize, 0x000c, "cmd1 block size");
        assertEquals(cmd1.payloadHex    , "000C3E74435076490004C0D5".toUpperCase(), "cmd1 payload");
    }

    @Test
    public void testCmdVer() throws Exception {
        ByteBuffer buf = Utils.parseHexString("000CA7B45F76657200020010");
        CmdFirmwareVersion cmd = (CmdFirmwareVersion)Command.read(buf);
        assertFalse(buf.hasRemaining());
        assertEquals(cmd.major, 2, "major");
        assertEquals(cmd.minor, 16, "minor");
        assertEquals(cmd.version, "2.16", "version");
    }

    @Test
    public void testCmdPin() throws Exception {
        ByteBuffer buf = Utils.parseHexString("003481985F70696E4154454D2054656C65766973696F6E2053747564696F007450888198507E2480800000A45009A7B401000000");
        CmdProductId cmd = (CmdProductId)Command.read(buf);
        assertFalse(buf.hasRemaining());
        assertEquals(cmd.name, "ATEM Television Studio");
    }

    @Test
    public void testCmdTop() throws Exception {
        ByteBuffer buf = Utils.parseHexString("001400A45F746F70011202000200000001000138");
        CmdTopology cmd = (CmdTopology)Command.read(buf);
        assertFalse(buf.hasRemaining());
        assertEquals(cmd.mes, 1, "mes");
        assertEquals(cmd.sources, 18, "sources");
        assertEquals(cmd.colorGenerators, 2, "colorGenerators");
        assertEquals(cmd.auxBusses, 0, "auxBusses");
        assertEquals(cmd.downstreamKeyes, 2, "downstreamKeyes");
        assertEquals(cmd.stingers, 0, "stingers");
        assertEquals(cmd.dves, 0, "dves");
        assertEquals(cmd.superSources, 0, "superSources");
        assertEquals(cmd.uc1, 1, "uc1");
        assertFalse(cmd.hasSdOutput, "hasSdOutput");
        assertEquals(cmd.uc2, 1, "uc2");
        assertEquals(cmd.uc3, 56, "uc3");
    }

    @Test
    public void testCmdPrgi() throws Exception {
        ByteBuffer buf = Utils.parseHexString("000C7BA45072674900790001");
        CmdProgramInput cmd = (CmdProgramInput)Command.read(buf);
        assertFalse(buf.hasRemaining());
        assertEquals(cmd.me, 0, "me");
        assertEquals(cmd.uc1, 121, "uc1");
        assertEquals(cmd.videoSource, 1, "videoSource");
    }

    @Test
    public void testCmdPrvi() throws Exception {
        ByteBuffer buf = Utils.parseHexString("0010006550727649008800060000000A");
        CmdPreviewInput cmd = (CmdPreviewInput)Command.read(buf);
        assertFalse(buf.hasRemaining());
        assertEquals(cmd.me, 0, "me");
        assertEquals(cmd.uc1, 136, "uc1");
        assertEquals(cmd.videoSource, 6, "videoSource");
        assertEquals(cmd.uc2, 10, "uc2");
    }

    @Test
    public void testCmdTlin() throws Exception {
        ByteBuffer buf = Utils.parseHexString("00104F6E546C496E0006010000000002");
        CmdTallyByIndex cmd = (CmdTallyByIndex)Command.read(buf);
        assertFalse(buf.hasRemaining());

        assertEquals(cmd.length, 6, "cmd length");

        assertTrue(cmd.isOnProgram(0));
        assertFalse(cmd.isOnPreview(0));

        assertFalse(cmd.isOnProgram(1));
        assertFalse(cmd.isOnPreview(1));

        assertFalse(cmd.isOnProgram(2));
        assertFalse(cmd.isOnPreview(2));

        assertFalse(cmd.isOnProgram(3));
        assertFalse(cmd.isOnPreview(3));

        assertFalse(cmd.isOnProgram(4));
        assertFalse(cmd.isOnPreview(4));

        assertFalse(cmd.isOnProgram(5));
        assertTrue(cmd.isOnPreview(5));
    }


    @Test
    public void testCmdTlc() throws Exception {
        ByteBuffer buf = Utils.parseHexString("001000025F546C4300010000060C9828");
        CmdTallyChannelConfig cmd = (CmdTallyChannelConfig)Command.read(buf);
        assertFalse(buf.hasRemaining());
        assertEquals(cmd.tallyChannels, 6, "tallyChannels");
        assertEquals(cmd.uc1, 0x010000, "uc1");
        assertEquals(cmd.uc2, 0x0C9828, "uc2");
    }

    @Test
    public void testCmdInpr() throws Exception {
        ByteBuffer buf = Utils.parseHexString("002C0000496E50720000426C61636B005019A720500A673400000000500C426C6B0001000100010012010007");
        CmdInputProperties cmd = (CmdInputProperties)Command.read(buf);
        assertFalse(buf.hasRemaining());
        assertEquals(cmd.videoSource, 0, "videoSource");
        assertEquals(cmd.longName, "Black", "longName");
        assertEquals(cmd.shortName, "Blk", "shortName");

        //TODO: add all fields
    }


    @Test
    public void testCmdAmtl() throws Exception {
        ByteBuffer buf = Utils.parseHexString("0020 AAF0 414D546C 0007 000100 000200 000301 000400 000500 000600 044D01 00");
        CmdAudioMixerTally cmd = (CmdAudioMixerTally) Command.read(buf);
        assertFalse(buf.hasRemaining());

        assertEquals(cmd.length, 7, "length");
        assertFalse(cmd.isMixedIn(1));
        assertFalse(cmd.isMixedIn(2));
        assertTrue(cmd.isMixedIn(3));
        assertFalse(cmd.isMixedIn(4));
        assertFalse(cmd.isMixedIn(5));
        assertFalse(cmd.isMixedIn(6));
        assertTrue(cmd.isMixedIn(1101));
    }

    @Test
    public void testCmdTlsr() throws Exception {
        ByteBuffer buf = Utils.parseHexString("00400000546C5372001200000000010100020000030000040000050000060203E80007D10007D2000BC2000BC3000BCC000BCD00271A00271B001B59001B5A00");
        CmdTallyBySource cmd = (CmdTallyBySource)Command.read(buf);
        assertFalse(buf.hasRemaining());
        assertEquals(cmd.length, 18, "length");
        assertFalse(cmd.isOnProgram(0));
        assertFalse(cmd.isOnPreview(0));
        assertTrue(cmd.isOnProgram(1));
        assertFalse(cmd.isOnPreview(1));
        assertFalse(cmd.isOnProgram(2));
        assertFalse(cmd.isOnPreview(2));
        assertFalse(cmd.isOnProgram(3));
        assertFalse(cmd.isOnPreview(3));
        assertFalse(cmd.isOnProgram(4));
        assertFalse(cmd.isOnPreview(4));
        assertFalse(cmd.isOnProgram(5));
        assertFalse(cmd.isOnPreview(5));
        assertFalse(cmd.isOnProgram(6));
        assertTrue(cmd.isOnPreview(6));
        assertFalse(cmd.isOnProgram(1000));
        assertFalse(cmd.isOnPreview(1000));
        assertFalse(cmd.isOnProgram(2001));
        assertFalse(cmd.isOnPreview(2002));
        assertFalse(cmd.isOnProgram(3010));
        assertFalse(cmd.isOnPreview(3011));
        assertFalse(cmd.isOnProgram(3020));
        assertFalse(cmd.isOnPreview(3020));
        assertFalse(cmd.isOnProgram(3021));
        assertFalse(cmd.isOnPreview(3021));
        assertFalse(cmd.isOnProgram(10010));
        assertFalse(cmd.isOnPreview(10010));
        assertFalse(cmd.isOnProgram(10011));
        assertFalse(cmd.isOnPreview(10011));
        assertFalse(cmd.isOnProgram(7001));
        assertFalse(cmd.isOnPreview(7001));
        assertFalse(cmd.isOnProgram(7002));
        assertFalse(cmd.isOnPreview(7002));
    }
}
