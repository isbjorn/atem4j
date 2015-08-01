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
 * Created by Oleg Akimov on 25/07/15.
 */
public class CmdPreviewInput extends Command {
    public final int me;
    public final int uc1;
    public final int videoSource;
    public final int uc2;

    public CmdPreviewInput(int me, int uc1, int videoSource, int uc2) {
        this.me = me;
        this.uc1 = uc1;
        this.videoSource = videoSource;
        this.uc2 = uc2;
    }

    @Override
    public String toString() {
        return String.format("%s me=%d videoSource=%d", getClass().getSimpleName(), me, videoSource);
    }
}
