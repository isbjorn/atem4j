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
public class CmdTopology extends Command {
    public final int mes;
    public final int sources;
    public final int colorGenerators;
    public final int auxBusses;
    public final int downstreamKeyes;
    public final int stingers;
    public final int dves;
    public final int superSources;
    public final int uc1;
    public final boolean hasSdOutput;
    public final int uc2;
    public final int uc3;

    public CmdTopology(
            int mes,
            int sources,
            int colorGenerators,
            int auxBusses,
            int downstreamKeyes,
            int stingers,
            int dves,
            int superSources,
            int uc1,
            boolean hasSdOutput,
            int uc2,
            int uc3
    ) {
        this.mes = mes;
        this.sources = sources;
        this.colorGenerators = colorGenerators;
        this.auxBusses = auxBusses;
        this.downstreamKeyes = downstreamKeyes;
        this.stingers = stingers;
        this.dves = dves;
        this.superSources = superSources;
        this.uc1 = uc1;
        this.hasSdOutput = hasSdOutput;
        this.uc2 = uc2;
        this.uc3 = uc3;
    }

    @Override
    public String toString() {
        return String.format(
                "%s mes=%d sources=%d colorGenerators=%d auxBusses=%d downstreamKeyes=%d stingers=%d dves=%d superSources=%d uc1=%d hasSdOutput=%b uc2=%d uc3=%d",
                getClass().getSimpleName(),
                mes,
                sources,
                colorGenerators,
                auxBusses,
                downstreamKeyes,
                stingers,
                dves,
                superSources,
                uc1,
                hasSdOutput,
                uc2,
                uc3
        );
    }
}
