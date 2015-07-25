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
import com.sun.javaws.exceptions.InvalidArgumentException;

/**
 * Created by Oleg Akimov on 25/07/15.
 */
public class CmdTallyByIndex extends Command {

    public static final int FLAG_PROGRAM = 0b01;
    public static final int FLAG_PREVIEW = 0b10;

    public final byte[] flags;
    public final int length;

    public CmdTallyByIndex(@NotNull byte[] flags) {
        if (flags == null) {
            throw new IllegalArgumentException("flags must be not null");
        }

        this.flags = flags;
        this.length = flags.length;
    }

    public boolean isOnProgram(int index) {
        if (index < 0 || index >= length) {
            throw new IllegalArgumentException(String.format("index = %d must be in [0,%d]", length - 1));
        }

        return (flags[index] & FLAG_PROGRAM) != 0;
    }

    public boolean isOnPreview(int index) {
        if (index < 0 || index >= length) {
            throw new IllegalArgumentException(String.format("index = %d must be in [0,%d]", length - 1));
        }

        return (flags[index] & FLAG_PREVIEW) != 0;
    }

    @Override
    public String toString() {
        String info = "CmdTallyByIndex";

        for(int j = 0; j < flags.length; j++) {
            info = String.format("%s %d=%s%s",
                    info,
                    j,
                    ((flags[j] & FLAG_PROGRAM) == 0) ? "" : "PGM",
                    ((flags[j] & FLAG_PREVIEW) == 0) ? "" : "PVW"
            );
        }

        return info;
    }
}
