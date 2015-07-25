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
        int payloadSize = blockSize - 2;

        if (blockSize < 2) {
            throw new IllegalArgumentException("buf contains broken payload block with blockSize = %d");
        }

        if (buf.remaining() < payloadSize) {
            throw new IllegalArgumentException(String.format(
                    "buf remaining = %d, this is to short for payload with blockSize: %d",
                    buf.remaining(),
                    payloadSize
            ));
        }

        byte[] payload = new byte[payloadSize];
        buf.get(payload);

        ByteBuffer body = ByteBuffer.wrap(payload);
        int divStart = body.getInt();
        int command = body.getInt();
        //int divEnd = body.getInt();

        Command cmd;
        switch (command) {
            default:
                String payloadHex = DatatypeConverter.printHexBinary(payload).toUpperCase();
                cmd = new CmdUnknown(blockSize, payloadHex);
                break;
        }

        return cmd;
    }
}