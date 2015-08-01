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

import java.net.PortUnreachableException;

/**
 * Created by Oleg Akimov on 31/07/15.
 */
public interface IUdpClientListener {

    void onPacketReceived(Packet packet);

    void onPortUnreachableException(PortUnreachableException ex);

    void onClientClose();

    void onClientStop();

    void onParseException(ParseException ex);
}