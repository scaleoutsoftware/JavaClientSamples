/*
 * (C) Copyright 2025 by ScaleOut Software, Inc.
 *
 * LICENSE AND DISCLAIMER
 * ----------------------
 * This material contains sample programming source code ("Sample Code").
 * ScaleOut Software, Inc. (SSI) grants you a nonexclusive license to compile,
 * link, run, display, reproduce, and prepare derivative works of
 * this Sample Code.  The Sample Code has not been thoroughly
 * tested under all conditions.  SSI, therefore, does not guarantee
 * or imply its reliability, serviceability, or function. SSI
 * provides no support services for the Sample Code.
 *
 * All Sample Code contained herein is provided to you "AS IS" without
 * any warranties of any kind. THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGMENT ARE EXPRESSLY
 * DISCLAIMED.  SOME JURISDICTIONS DO NOT ALLOW THE EXCLUSION OF IMPLIED
 * WARRANTIES, SO THE ABOVE EXCLUSIONS MAY NOT APPLY TO YOU.  IN NO
 * EVENT WILL SSI BE LIABLE TO ANY PARTY FOR ANY DIRECT, INDIRECT,
 * SPECIAL OR OTHER CONSEQUENTIAL DAMAGES FOR ANY USE OF THE SAMPLE CODE
 * INCLUDING, WITHOUT LIMITATION, ANY LOST PROFITS, BUSINESS
 * INTERRUPTION, LOSS OF PROGRAMS OR OTHER DATA ON YOUR INFORMATION
 * HANDLING SYSTEM OR OTHERWISE, EVEN IF WE ARE EXPRESSLY ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGES.
 */
package com.scaleout.client.samples.streaming;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import java.io.IOException;

public class StockQuote {
    private final  String _ticker;
    private final double _price;
    public final long _volume;
    public final long _timestampMs;

    public StockQuote(String ticker, double price, long volume, long timestampMs) {
        _ticker = ticker;
        _price = price;
        _volume = volume;
        _timestampMs = timestampMs;
    }

    public String getTicker() {
        return _ticker;
    }

    public double getPrice() {
        return _price;
    }

    public long getVolume() {
        return _volume;
    }

    public long getTimestampMs() {
        return _timestampMs;
    }

    public static byte[] toBytes(StockQuote stockQuote) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packString(stockQuote.getTicker())
                .packDouble(stockQuote.getPrice())
                .packLong(stockQuote.getVolume())
                .packLong(stockQuote.getTimestampMs())
                .close();
        return packer.toByteArray();
    }

    public static StockQuote fromBytes(byte[] bytes) throws IOException {
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
        String ticker = unpacker.unpackString();
        double price = unpacker.unpackDouble();
        long volume = unpacker.unpackLong();
        long timestampMs = unpacker.unpackLong();
        unpacker.close();
        return new StockQuote(ticker, price, volume, timestampMs);
    }
}
