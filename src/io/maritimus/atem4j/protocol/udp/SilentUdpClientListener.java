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

package io.maritimus.atem4j.protocol.udp;

import io.maritimus.atem4j.protocol.Packet;
import io.maritimus.atem4j.protocol.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.PortUnreachableException;

/**
 * Created by Oleg Akimov on 31/07/15.
 */
public class SilentUdpClientListener implements IUdpClientListener {

    private static final Logger log = LoggerFactory.getLogger(SilentUdpClientListener.class);

    @Override
    public void onPacketReceived(Packet packet) {
        log.debug(String.format("onPacketReceived %s", packet.toString()));
    }

    @Override
    public void onPortUnreachableException(PortUnreachableException ex) {
        log.error("onPortUnreachableException", ex);
    }

    @Override
    public void onClientClose() {
        log.debug("onClientClose");
    }

    @Override
    public void onClientStop() {
        log.debug("onClientStop");
    }

    @Override
    public void onParseException(ParseException ex) {
        log.error("onParseException", ex);
    }
}
