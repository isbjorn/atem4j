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

import java.nio.ByteBuffer;

/**
 * Created by Oleg Akimov on 26/07/15.
 */
public class CmdAudioMixerTally extends Command {

    public static final int STATUS_NONE = 0;
    public static final int STATUS_MIXED_IN = 1;

    public final int length;
    public final int[][] statuses;

    public CmdAudioMixerTally(int length, int[][] statuses) {
        this.length = length;
        this.statuses = statuses;
    }

    public int getStatus(int audioSource) {
        for (int j = 0; j < statuses.length; j++) {
            if (statuses[j][0] == audioSource) {
                return statuses[j][1];
            }
        }
        return STATUS_NONE;
    }

    public boolean isMixedIn(int audioSource) {
        return (getStatus(audioSource) & STATUS_MIXED_IN) != 0;
    }

    public int[] getAudioSources() {
        int[] sources = new int[length];
        for (int j = 0; j < statuses.length; j++) {
            sources[j] = statuses[j][0];
        }
        return sources;
    }

    @Override
    public String toString() {
        String stats = "";
        for (int audioSource : getAudioSources()) {
            stats = String.format("%s %d=%s", stats, audioSource, isMixedIn(audioSource) ? "on" : "off");
        }
        return String.format(
                "%s length=%d%s",
                getClass().getSimpleName(),
                length,
                stats
        );
    }

    public static CmdAudioMixerTally read(ByteBuffer body) {
        int length = body.getChar();
        int[][] statuses = new int[length][2];
        for (int j = 0; j < length; j++) {
            statuses[j][0] = body.getChar();
            statuses[j][1] = body.get() & 0xFF;
        }

        return new CmdAudioMixerTally(length, statuses);
    }
}
