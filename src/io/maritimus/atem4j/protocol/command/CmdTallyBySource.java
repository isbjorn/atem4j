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

import java.nio.ByteBuffer;

/**
 * Created by Oleg Akimov on 26/07/15.
 */
public class CmdTallyBySource extends Command {

    public static final int STATUS_NONE = 0;
    public static final int STATUS_PROGRAM = 0b01;
    public static final int STATUS_PREVIEW = 0b10;

    public final int length;
    public final int[][] statuses;

    public CmdTallyBySource(int length, int[][] statuses) {
        this.length = length;
        this.statuses = statuses;
    }

    public int getStatus(int videoSource) {
        for (int j = 0; j < statuses.length; j++) {
            if (statuses[j][0] == videoSource) {
                return statuses[j][1];
            }
        }
        return STATUS_NONE;
    }

    public boolean isOnPreview(int videoSource) {
        return (getStatus(videoSource) & STATUS_PREVIEW) != 0;
    }

    public boolean isOnProgram(int videoSource) {
        return (getStatus(videoSource) & STATUS_PROGRAM) != 0;
    }

    public int[] getVideoSources() {
        int[] sources = new int[length];
        for (int j = 0; j < statuses.length; j++) {
            sources[j] = statuses[j][0];
        }
        return sources;
    }

    public boolean hasVideoSource(int videoSource) {
        for (int j = 0; j < statuses.length; j++) {
            if (videoSource == statuses[j][0]) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals (Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof CmdTallyBySource)) {
            return false;
        }

        CmdTallyBySource that = (CmdTallyBySource)object;

        if (length != that.length) {
            return false;
        }

        int[] sources = getVideoSources();

        for (int j = 0; j < sources.length; j++) {
            if (!that.hasVideoSource(sources[j])) {
                return false;
            }

            if (getStatus(j) != that.getStatus(j)) {
                return false;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        String stats = "";
        for (int videoSource : getVideoSources()) {
            stats = String.format(
                    "%s %d=%s%s",
                    stats,
                    videoSource,
                    isOnProgram(videoSource) ? "PGM" : "",
                    isOnPreview(videoSource) ? "PVW" : ""
            );
        }
        return String.format(
                "%s length=%d%s",
                this.getClass().getSimpleName(),
                length,
                stats
        );
    }

    public static CmdTallyBySource read(ByteBuffer body) {
        int length = body.getChar();
        int[][] statuses = new int[length][2];
        for (int j = 0; j < length; j++) {
            statuses[j][0] = body.getChar();
            statuses[j][1] = body.get() & 0xFF;
        }

        return new CmdTallyBySource(length, statuses);
    }
}
