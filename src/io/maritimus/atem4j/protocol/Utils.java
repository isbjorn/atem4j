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

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Oleg Akimov on 26/07/15.
 */
public class Utils {

    public static String readString(ByteBuffer buf, int maxLength) {
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
}
