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

/**
 * Created by Oleg Akimov on 25/07/15.
 */
public class CmdUnknown extends Command {
    public final int blockSize;  // block blockSize (payload + 2 bytes for blockSize + N bytes for dividers)
    public final String payloadHex;

    public CmdUnknown(int blockSize, @NotNull String payloadHex) {
        if (blockSize <= 0) {
            throw new IllegalArgumentException(String.format("blockSize must be positive, the given blockSize is %d", blockSize));
        }

        if (payloadHex == null) {
            throw new IllegalArgumentException("payloadHex must be not null");
        }

        this.blockSize = blockSize;
        this.payloadHex = payloadHex;
    }

    @Override
    public String toString() {
        return String.format("CmdUnknown size=%d payload=%s", blockSize, payloadHex);
    }
}
