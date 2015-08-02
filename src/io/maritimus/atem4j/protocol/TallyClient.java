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
import io.maritimus.atem4j.protocol.command.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Oleg Akimov on 02/08/15.
 */
public class TallyClient extends AtemClient {
    private static final Logger log = LoggerFactory.getLogger(TallyClient.class);
    private long uuid = 0;

    private int sources = 0;
    private String firmwareVersion = "unknown";
    private String productName = "unknown";
    private ConcurrentMap<Integer, String> shortNames = new ConcurrentHashMap<>(16);
    private ConcurrentMap<Integer, String> longNames = new ConcurrentHashMap<>(16);
    private int previewIndex = 0;
    private int programIndex = 0;
    private CmdTallyBySource tally = null;

    public static TallyClient create(InetSocketAddress atemAddress) {
        TallyClient client = new TallyClient(atemAddress);
        client.toStopped();
        return client;
    }

    protected TallyClient(InetSocketAddress atemAddress) {
        super(atemAddress);
    }

    public String getShortName(Integer index) {
        return shortNames.getOrDefault(index, "?" + index);
    }

    public String getLongName(Integer index) {
        return shortNames.getOrDefault(index, "Input " + index);
    }

    public JSONObject toJson() {
        JSONObject retJson = new JSONObject();

        JSONArray info = new JSONArray();

        if (tally != null) {
            for (int index : tally.getVideoSources()) {
                JSONObject input = new JSONObject();
                input.put("index", index).
                        put("name", getShortName(index)).
                        put("long", getLongName(index)).
                        put("pgm", tally.isOnProgram(index)).
                        put("prv", tally.isOnPreview(index));
                info.put(input);
            }
        }

        retJson.
                put("uuid", uuid).
                put("rus", "Проверка").
                put("host", getAtemHost()).
                put("state", getState()).
                put("firmware", firmwareVersion).
                put("product", productName).
                put("sources", sources).
                put("pgm", programIndex).
                put("prv", previewIndex).
                put("tally", info);

        return retJson;
    }

    @Override
    public String toString() {
        StringBuffer info = new StringBuffer();

        if (tally != null) {
            for (int index : tally.getVideoSources()) {
                info.append(String.format(
                        "\n\t\t%5d\t%5s\t%s%s\t(%s)",
                        index,
                        getShortName(index),
                        tally.isOnProgram(index) ? " PGM" : "    ",
                        tally.isOnPreview(index) ? " PRV" : "    ",
                        getLongName(index)
                ));
            }
        }

        return String.format(
                "TallyClient" +
                        "\n\tuuid = %d" +
                        "\n\thost = %s" +
                        "\n\tstate = %s" +
                        "\n\tfirmware = %s" +
                        "\n\tproduct = %s" +
                        "\n\t" +
                        "\n\tsources = %d" +
                        "\n\tpgm = %d" +
                        "\n\tprv = %d" +
                        "\n\t" +
                        "%s\n\n%s",
                uuid,
                getAtemHost(),
                getState(),
                firmwareVersion,
                productName,
                sources,
                programIndex,
                previewIndex,
                info,
                toJson().toString(2)
        );
    }

    protected void up() {
        uuid = uuid + 1;
        log.info(toString());
    }

    protected void setTally(@NotNull CmdTallyBySource tally) {
        if (tally == null) {
            throw new IllegalArgumentException("tally must be not null");
        }
        if (tally.equals(this.tally)) {
            return;
        }

        this.tally = tally;
        up();
    }

    protected void setPreviewIndex(int previewIndex) {
        if (this.previewIndex == previewIndex) {
            return;
        }

        this.previewIndex = previewIndex;
        up();
    }

    protected void setProgramIndex(int programIndex) {
        if (this.programIndex == programIndex) {
            return;
        }

        this.programIndex = programIndex;
        up();
    }

    public void setNames(Integer index, String shortName, String longName) {
        if (shortNames.containsKey(index)
                && shortNames.get(index) == shortName
                && longNames.containsKey(index)
                && longNames.get(index) == longName
                ) {
            return;
        }

        shortNames.put(index, shortName);
        longNames.put(index, longName);
        up();
    }

    public void setProductName(String productName) {
        if (this.productName == productName) {
            return;
        }

        this.productName = productName;
        up();
    }

    protected void setSources(int sources) {
        if (this.sources == sources) {
            return;
        }

        this.sources = sources;
        up();
    }

    protected void setFirmwareVersion(String firmwareVersion) {
        if (this.firmwareVersion == firmwareVersion) {
            return;
        }

        this.firmwareVersion = firmwareVersion;
        up();
    }

    @Override
    protected void setState(String newState) {
        super.setState(newState);
        up();
    }

    @Override
    protected void applyCommand(Command cmd) {
        if (false) {

        } else if (cmd instanceof CmdTallyBySource) {

            setTally((CmdTallyBySource) cmd);

        } else if (cmd instanceof CmdProgramInput) {

            setProgramIndex(((CmdProgramInput) cmd).videoSource);

        } else if (cmd instanceof CmdPreviewInput) {

            setPreviewIndex(((CmdPreviewInput) cmd).videoSource);

        } else if (cmd instanceof CmdInputProperties) {

            CmdInputProperties props = (CmdInputProperties) cmd;
            setNames(props.videoSource, props.shortName, props.longName);

        } else if (cmd instanceof CmdTopology) {

            setSources(((CmdTopology) cmd).sources);

        } else if (cmd instanceof CmdFirmwareVersion) {

            setFirmwareVersion(((CmdFirmwareVersion) cmd).version);

        } else if (cmd instanceof CmdProductId) {

            setProductName(((CmdProductId) cmd).name);

        }
    }

}
