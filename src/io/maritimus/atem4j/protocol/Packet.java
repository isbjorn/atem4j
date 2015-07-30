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
import java.util.List;

/**
 * Created by Oleg Akimov on 26/07/15.
 */
public class Packet {
    public final PacketHeader header;
    public final List<Command> commands;

    public Packet(@NotNull PacketHeader header, @NotNull List<Command> commands) {
        if (header == null) {
            throw new IllegalArgumentException("header must be not null");
        }
        if (commands == null) {
            throw new IllegalArgumentException("commands must be not null");
        }
        this.header = header;
        this.commands = commands;
    }

    public static Packet read(@NotNull ByteBuffer buf) throws ParseException {
        if (buf == null) {
            throw new IllegalArgumentException("buf must be not null");
        }

        try {
            PacketHeader header = PacketHeader.read(buf);

            if (buf.remaining() != header.size - PacketHeader.HEADER_LENGTH) {
                throw new ParseException(
                        "Packet header size = %d, but buf remaining = %d",
                        header.size,
                        buf.remaining()
                );
            }

            if (header.isHello()) {
                buf.mark();
                int connectionFlag = buf.get() & 0xFF;
                buf.position(buf.position() + 2);   // skip 2 bytes
                int connectionUptime = buf.get() & 0xFF;
                buf.reset();
                String payload = Utils.readHexString(buf);
                return new PacketHello(header, connectionFlag, connectionUptime, payload);
            }

            ArrayList<Command> commands = new ArrayList<>();
            while (buf.hasRemaining()) {
                commands.add(Command.read(buf));
            }
            return new Packet(header, commands);

        } catch (ParseException ex) {
            throw ex;
        } catch (Throwable ex) {
            buf.rewind();
            throw new ParseException(
                    "Can't parse packet %s cause ",
                    Utils.readHexString(buf),
                    ex.getMessage());
        }
    }

    public void write(@NotNull ByteBuffer buf) {
        if (buf == null) {
            throw new IllegalArgumentException("buf must be not null");
        }

        header.write(buf);
        for (Command command : commands) {
            command.write(buf);
        }
    }
}
