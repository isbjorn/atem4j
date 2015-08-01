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

package io.maritimus.atem4j.protocol.command;

import io.maritimus.atem4j.protocol.command.Command;

/**
 * Created by Oleg Akimov on 26/07/15.
 */
public class CmdTallyChannelConfig extends Command {
    public final int uc1;   // 1 byte
    public final int tallyChannels;
    public final int uc2;   // 3 bytes

    public CmdTallyChannelConfig(int uc1, int tallyChanells, int uc2) {
        this.uc1 = uc1;
        this.tallyChannels = tallyChanells;
        this.uc2 = uc2;
    }

    @Override
    public String toString() {
        return String.format(
                "%s tallyChannels=%d, uc1=%s uc2=%s",
                this.getClass().getSimpleName(),
                tallyChannels,
                Integer.toBinaryString(uc1),
                Integer.toBinaryString(uc2)
        );
    }
}
