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
public class CmdSetPreviewInput extends Command {
    public final int me;
    public final int videoSource;

    public CmdSetPreviewInput(int me, int videoSource) {
        this.me = me;
        this.videoSource = videoSource;
    }

    @Override
    public String toString() {
        return String.format("%s me=%d videoSource=%d", getClass().getSimpleName(), me, videoSource);
    }
}
