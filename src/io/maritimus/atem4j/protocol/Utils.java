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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Oleg Akimov on 26/07/15.
 */
public class Utils {

    public static final Random rand = new Random();

    public static int random(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    public static String readString(@NotNull ByteBuffer buf, int maxLength) throws ParseException {
        if (buf == null) {
            throw new IllegalArgumentException("buf must be not null");
        }

        if (maxLength <=0 ) {
            throw new IllegalArgumentException("maxLength must be > 0");
        }

        if (buf.remaining() < maxLength) {
            throw new ParseException(
                    "Can't read string maxLength = %d from buf with remaining = %d",
                    maxLength,
                    buf.remaining()
            );
        }

        byte[] strBuf = new byte[maxLength];
        buf.get(strBuf);
        int k;
        for (k = 0; k < strBuf.length; k++) {
            if (strBuf[k] == 0) {
                break;
            }
        }
        strBuf = Arrays.copyOf(strBuf, k);
        String str;

        try {
            str = new String(strBuf, "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            str = DatatypeConverter.printHexBinary(strBuf);
        }

        return str;
    }

    public static String stringifyCommand(int rawCommand) {
        String command = "" +
                (char)((rawCommand >> 24) & 0xFF) +
                (char)((rawCommand >> 16) & 0xFF) +
                (char)((rawCommand >> 8) & 0xFF) +
                (char)(rawCommand & 0xFF);
        return command;
    }

    public static ByteBuffer parseHexString(String str) {
        return ByteBuffer.wrap(DatatypeConverter.parseHexBinary(str.replace(" ", "")));
    }

    public static String readHexString(ByteBuffer buf) {
        return readHexString(buf, 0);
    }

    public static String readHexString(ByteBuffer buf, int length) {
        byte[] block = new byte[length == 0 ? buf.remaining() : length];
        buf.get(block);
        return DatatypeConverter.printHexBinary(block).toUpperCase();
    }
}
