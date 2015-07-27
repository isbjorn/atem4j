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

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;

/**
 * Created by Oleg Akimov on 25/07/15.
 */
public abstract class Command {

    public static Command read(@NotNull ByteBuffer buf) throws ParseException {
        if (buf == null) {
            throw new IllegalArgumentException("buf must be not null");
        }

        if (buf.remaining() < 2) {
            throw new ParseException(
                    "buf remaining = %d is to short for next payload",
                    buf.remaining()
            );
        }

        buf.mark();
        
        int blockSize = buf.getChar();
        int payloadSize = blockSize - 2 /* size */ - 2 /* div start */ - 4 /* command */;
        int finalPosition = buf.position() + blockSize - 2;

        if (blockSize < 2) {
            throw new ParseException(
                    "buf contains broken payload block with blockSize = %d",
                    blockSize
            );
        }

        if (buf.remaining() < blockSize - 2) {
            int remaining = buf.remaining();
            String payloadHex = DatatypeConverter.printHexBinary(buf.array()).toUpperCase();
            throw new ParseException(
                    "buf remaining = %d is to short for payload with blockSize = %d and payloadSize = %d : %s",
                    remaining,
                    blockSize,
                    payloadSize,
                    payloadHex
            );
        }

        int divStart = buf.getChar();
        int rawCommand = buf.getInt();
        String command = Utils.stringifyCommand(rawCommand);
        Command cmd;

        switch (command) {
            /*
            case "CPvI":
                // 4 bytes
                int me = body.get() & 0xFF;
                int uc1 = body.get() & 0xFF;
                int videoSource = body.getChar();
                cmd = new CmdSetPreviewInput(me, videoSource);
                break;
            */

            case "_ver":
                int major = buf.getChar();
                int minor = buf.getChar();
                cmd = new CmdFirmwareVersion(major, minor);
                break;

            case "_pin":
                String name = Utils.readString(buf, payloadSize);
                cmd = new CmdProductId(name);
                break;

            case "_top":
                cmd = new CmdTopology(
                  buf.get() & 0xFF,  // int mes,
                  buf.get() & 0xFF,  // int sources,
                  buf.get() & 0xFF,  // int colorGenerators,
                  buf.get() & 0xFF,  // int auxBusses,
                  buf.get() & 0xFF,  // int downstreamKeyes,
                  buf.get() & 0xFF,  // int stingers,
                  buf.get() & 0xFF,  // int dves,
                  buf.get() & 0xFF,  // int superSources,
                  buf.get() & 0xFF,  // int uc1,
                  (buf.get() & 0b1) != 0,  // boolean hasSdOutput,
                  buf.get() & 0xFF,  // int uc2,
                  buf.get() & 0xFF   // int uc3
                );
                break;

            case "PrgI":
                cmd = new CmdProgramInput(
                        buf.get() & 0xFF,
                        buf.get() & 0xFF,
                        buf.getChar()
                );
                break;

            case "PrvI":
                cmd = new CmdPreviewInput(
                        buf.get() & 0xFF,
                        buf.get() & 0xFF,
                        buf.getChar(),
                        buf.getInt()
                );
                break;

            case "TlIn":
                int length = buf.getChar();    // 0 - 20, remaining must = (length * 1 byte)
                byte[] flags = new byte[length];
                buf.get(flags);
                cmd = new CmdTallyByIndex(flags);
                break;

            case "_TlC":
                int uc1 = buf.getInt();
                int tallyChannels = buf.get() & 0xFF;
                int uc2 = ((buf.get() & 0xFF) << 16) | buf.getChar();
                cmd = new CmdTallyChannelConfig(uc1, tallyChannels, uc2);
                break;

            case "InPr":
                cmd = CmdInputProperties.read(buf);
                break;

            case "AMTl":
                cmd = CmdAudioMixerTally.read(buf);
                if (buf.position() < finalPosition) {
                    buf.position(finalPosition);
                }

                break;

            case "TlSr":
                cmd = CmdTallyBySource.read(buf);
                break;

            default:
                byte[] payload = new byte[blockSize];
                buf.reset();
                buf.get(payload);
                String payloadHex = DatatypeConverter.printHexBinary(payload).toUpperCase();
                cmd = new CmdUnknown(command, blockSize, payloadHex);
                break;
        }

        // re-checking right buffer position
        if (buf.position() != finalPosition) {
            throw new ParseException(
                    "Command %s reader miss buf final position, expecting = %d, actual = %d",
                    command,
                    finalPosition,
                    buf.position()
            );
        }

        return cmd;
    }
}
