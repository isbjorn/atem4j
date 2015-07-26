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

/**
 * Created by Oleg Akimov on 26/07/15.
 */
public class CmdFirmwareVersion extends Command {
    public final int major;
    public final int minor;
    public final String version;

    public CmdFirmwareVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
        this.version = String.format("%d.%d", major, minor);
    }

    @Override
    public String toString() {
        return String.format("%s version=%s", getClass().getSimpleName(), version);
    }
}
