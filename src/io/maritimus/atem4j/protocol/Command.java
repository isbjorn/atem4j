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

    public static String stringify (int rawCommand) {
        String command = "" +
                (char)((rawCommand >> 24) & 0xFF) +
                (char)((rawCommand >> 16) & 0xFF) +
                (char)((rawCommand >> 8) & 0xFF) +
                (char)(rawCommand & 0xFF);
        return command;
    }

    public static Command read(@NotNull ByteBuffer buf) {
        if (buf == null) {
            throw new IllegalArgumentException("buf must be not null");
        }

        if (buf.remaining() < 2) {
            throw new IllegalArgumentException(String.format(
                    "buf remaining = %d is to short for next payload",
                    buf.remaining()));
        }

        int blockSize = buf.getChar();
        int payloadSize = blockSize - 2 /* size */ - 2 /* div start */ - 4 /* command */;

        if (blockSize < 2) {
            throw new IllegalArgumentException("buf contains broken payload block with blockSize = %d");
        }

        if (buf.remaining() < blockSize - 2) {
            int remaining = buf.remaining();
            String payloadHex = DatatypeConverter.printHexBinary(buf.array()).toUpperCase();
            throw new IllegalArgumentException(String.format(
                    "buf remaining = %d, this is to short for payload %s with blockSize = %d and payloadSize = %d",
                    remaining,
                    payloadHex,
                    blockSize,
                    payloadSize
            ));
        }

        byte[] payload = new byte[blockSize];
        buf.position(buf.position() - 2);
        buf.get(payload);

        ByteBuffer body = ByteBuffer.wrap(payload);
        int blockSize2 = body.getChar(); // == blockSize
        int divStart = body.getChar();
        int rawCommand = body.getInt();
        String command = stringify(rawCommand);

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
                int major = body.getChar();
                int minor = body.getChar();
                cmd = new CmdFirmwareVersion(major, minor);
                break;

            case "_pin":
                String name = Utils.readString(body, payloadSize);
                cmd = new CmdProductId(name);
                break;

            case "_top":
                cmd = new CmdTopology(
                  body.get() & 0xFF,  // int mes,
                  body.get() & 0xFF,  // int sources,
                  body.get() & 0xFF,  // int colorGenerators,
                  body.get() & 0xFF,  // int auxBusses,
                  body.get() & 0xFF,  // int downstreamKeyes,
                  body.get() & 0xFF,  // int stingers,
                  body.get() & 0xFF,  // int dves,
                  body.get() & 0xFF,  // int superSources,
                  body.get() & 0xFF,  // int uc1,
                  (body.get() & 0b1) != 0,  // boolean hasSdOutput,
                  body.get() & 0xFF,  // int uc2,
                  body.get() & 0xFF   // int uc3
                );
                break;

            case "PrgI":
                cmd = new CmdProgramInput(
                        body.get() & 0xFF,
                        body.get() & 0xFF,
                        body.getChar()
                );
                break;

            case "PrvI":
                cmd = new CmdPreviewInput(
                        body.get() & 0xFF,
                        body.get() & 0xFF,
                        body.getChar(),
                        body.getChar()
                );
                break;

            case "TlIn":
                int length = body.getChar();    // 0 - 20, remaining must = (length * 1 byte)
                byte[] flags = new byte[length];
                body.get(flags);
                cmd = new CmdTallyByIndex(flags);
                break;

            case "_TlC":
                int uc1 = body.getInt();
                int tallyChannels = body.get() & 0xFF;
                int uc2 = ((body.get() & 0xFF) << 16) | body.getChar();
                cmd = new CmdTallyChannelConfig(uc1, tallyChannels, uc2);
                break;

            case "InPr":
                cmd = CmdInputProperties.read(body);
                break;

            case "AMTl":
                cmd = CmdAudioMixerTally.read(body);
                break;

            case "TlSr":
                cmd = CmdTallyBySource.read(body);
                break;

            default:
                String payloadHex = DatatypeConverter.printHexBinary(payload).toUpperCase();
                cmd = new CmdUnknown(command, blockSize, payloadHex);
                break;
        }

        return cmd;
    }
}
