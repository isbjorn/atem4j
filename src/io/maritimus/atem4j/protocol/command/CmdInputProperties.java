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

import io.maritimus.atem4j.protocol.ParseException;
import io.maritimus.atem4j.protocol.Utils;

import java.nio.ByteBuffer;

/**
 * Created by Oleg Akimov on 26/07/15.
 */
public class CmdInputProperties extends Command {
    public final int videoSource;
    public final String longName;
    public final String shortName;
    public final int uc1;
    public final int availableExternalPortTypes;
    public final int uc2;
    public final int externalPortType;
    public final int portType;
    public final int uc3;
    public final int availability;
    public final int meAvailability;
    public final int available3;
    public final int uc4;

    public CmdInputProperties(
            int videoSource,
            String longName,
            String shortName,
            int uc1,
            int availableExternalPortTypes,
            int uc2,
            int externalPortType,
            int portType,
            int uc3,
            int availability,
            int meAvailability,
            int available3,
            int uc4
    ) {
        this.videoSource = videoSource;
        this.longName = longName;
        this.shortName = shortName;
        this.uc1 = uc1;
        this.availableExternalPortTypes = availableExternalPortTypes;
        this.uc2 = uc2;
        this.externalPortType = externalPortType;
        this.portType = portType;
        this.uc3 = uc3;
        this.availability = availability;
        this.meAvailability = meAvailability;
        this.available3 = available3;
        this.uc4 = uc4;
    }

    @Override
    public String toString() {
        return String.format(
                "%s videoSource=%d shortName=%s longName=%s",
                getClass().getSimpleName(),
                videoSource,
                shortName,
                longName
        );
    }

    public static CmdInputProperties read(ByteBuffer body) throws ParseException {
        int videoSource = body.getChar();
        String longName = Utils.readString(body, 20);
        String shortName = Utils.readString(body, 4);
        int uc1 = body.get() & 0xFF;
        int availableExternalPortTypes = body.get() & 0xFF;
        int uc2 = body.get() & 0xFF;
        int externalPortType = body.get() & 0xFF;
        int portType = body.get() & 0xFF;
        int uc3 = body.get() & 0xFF;
        int availability = body.get() & 0xFF;
        int meAvailability = body.get() & 0xFF;
        int available3 = body.get() & 0xFF;
        int uc4 = body.get() & 0xFF;

        return new CmdInputProperties(
                videoSource,
                longName,
                shortName,
                uc1,
                availableExternalPortTypes,
                uc2,
                externalPortType,
                portType,
                uc3,
                availability,
                meAvailability,
                available3,
                uc4
        );
    }
}
