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

import java.nio.ByteBuffer;

/**
 * Created by Oleg Akimov on 23/07/15.
 */
public class PacketHeader {
    public static final int HEADER_LENGTH = 12;

    public static final int FLAG_ACK    = 0b10000000; // send ACK for this package
    public static final int FLAG_RESEND = 0b00100000; // this is an resend packet
    public static final int FLAG_HELLO  = 0b00010000; // this is an hello packet
    public static final int FLAG_ACKREQ = 0b00001000; // request acknowledge for this packet

    public static final int MASK_SIZE     = 0x7FF;
    public static final int MASK_BITMASK  = 0xF8;
    public static final int MASK_2BYTES   = 0xFFFF;
    public static final long MASK_4BYTES   = 0xFFFFFFFFL;

    public final int bitmask;   // ~1 byte
    public final int size;      // ~2 bytes header length + payload length
    public final int uid;       // 2 bytes  unique identifier
    public final int ackId;     // 2 bytes
    public final long unicorn;  // 4 bytes
    public final int packageId; // 2 bytes


    public PacketHeader(int bitmask, int size, int uid, int ackId, int packageId, long unicorn) {
        if ((bitmask & ~MASK_BITMASK) != 0) {
            throw new IllegalArgumentException(String.format("bitmask=%d is out of range", bitmask));
        }
        if ((size & ~MASK_SIZE) != 0) {
            throw new IllegalArgumentException(String.format("blockSize=%d is out of range", size));
        }
        if ((uid & ~MASK_2BYTES) != 0) {
            throw new IllegalArgumentException(String.format("uid=%d is out of range", uid));
        }
        if ((ackId & ~MASK_2BYTES) != 0) {
            throw new IllegalArgumentException(String.format("ackId=%d is out of range", ackId));
        }
        if ((packageId & ~MASK_2BYTES) != 0) {
            throw new IllegalArgumentException(String.format("packageId=%d is out of range", packageId));
        }
        if ((unicorn & ~MASK_4BYTES) != 0) {
            throw new IllegalArgumentException(String.format("unicorn=%d is out of range", unicorn));
        }

        this.bitmask = bitmask;
        this.size = size;
        this.uid = uid;
        this.ackId = ackId;
        this.packageId = packageId;
        this.unicorn = unicorn;
    }

    public boolean isHello() {
        return (this.bitmask & FLAG_HELLO) != 0;
    }

    public boolean isAckRequest () {
        return (this.bitmask & FLAG_ACKREQ) != 0;
    }

    public boolean isResend() {
        return (this.bitmask & FLAG_RESEND) != 0;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s%s%s%s\t\t\t%s",
                (FLAG_ACK    & bitmask) != 0 && (size == HEADER_LENGTH) ? "        " : String.format("PKG=%3d "   , packageId),
                (FLAG_ACK    & bitmask) == 0 ? "" : String.format("ACK=%d "   , ackId),
                (FLAG_RESEND & bitmask) == 0 ? "" : String.format("RESEND "),
                (FLAG_HELLO  & bitmask) == 0 ? "" : String.format("HELLO "),
                (FLAG_ACKREQ & bitmask) == 0 ? "" : String.format("ACKREQ "),
                humanify()
        );
    }

    public String humanify() {
        return String.format("bitmask=%s blockSize=%d uid=%d ackId=%d packageId=%d unicorn=%s",
                Integer.toBinaryString(bitmask),
                size,
                uid,
                ackId,
                packageId,
                Long.toBinaryString(unicorn)
        );
    }

    public static PacketHeader read(@NotNull ByteBuffer buf) throws ParseException {

        if (buf == null) {
            throw new IllegalArgumentException("buf must be not null");
        }

        if (buf.remaining() < HEADER_LENGTH) {
            throw new ParseException("buf is to short for header, remaining = %d", buf.remaining());
        }

        int head = (int) buf.getChar();
        int bitmask = (head >> 8) & MASK_BITMASK;
        int size = head & MASK_SIZE;
        int uid = buf.getChar();
        int ackId = buf.getChar();
        long unicorn = buf.getInt() & MASK_4BYTES;
        int packageId = buf.getChar();

        return new PacketHeader(bitmask, size, uid, ackId, packageId, unicorn);
    }

    public void write(@NotNull ByteBuffer buf) {
        if (buf == null) {
            throw new IllegalArgumentException("buf must be not null");
        }

        if (buf.remaining() < HEADER_LENGTH) {
            throw new IllegalArgumentException(String.format(
                    "buf is to short for header, remaining = %d",
                    buf.remaining()
            ));
        }

        int head = ((bitmask & MASK_BITMASK) << 8) | (size & MASK_SIZE);
        buf.putChar((char)head);
        buf.putChar((char)uid);
        buf.putChar((char)ackId);
        buf.putInt((int)unicorn);
        buf.putChar((char)packageId);
    }

    /*
    public static void write(@NotNull PacketHeader header, @NotNull ByteBuffer buf) {

        if (header == null) {
            throw new IllegalArgumentException("header must be not null");
        }

        if (buf == null) {
            throw new IllegalArgumentException("buf  must be not null");
        }

        int head = (header.bitmask << 8) | header.blockSize;
        buf.putChar((char)head);
        buf.putChar((char)header.uid);
        buf.putChar((char)header.ackId);
        buf.putInt((int)header.unicorn);
        buf.putChar((char)header.packageId);
    }
    */
}

/*

c: select random uid, packageId = 0
c: send FLAG_HELLO to ip:9910

s: send info

c: save uid
c: dont' send ack before initialization done
c: Do not ack the configuration packets until their payload is 0 (means L is 12 decimal)


c: request for ack - send command with FLAG_ACKREQ and packageId
s: answer for request with ac - send command with FLAG_ACK and ackId

// fetch payload
 */
