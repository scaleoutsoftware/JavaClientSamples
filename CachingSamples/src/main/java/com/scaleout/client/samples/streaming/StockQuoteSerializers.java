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

import com.scaleout.client.caching.CacheDeserializer;
import com.scaleout.client.caching.CacheSerializer;
import com.scaleout.client.caching.DeserializationException;
import com.scaleout.client.caching.SerializationException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.LinkedList;

public class StockQuoteSerializers {
    public static class StockQuotesMessagePackSerializer extends CacheSerializer<LinkedList<StockQuote>> {
        @Override
        public byte[] serialize(LinkedList<StockQuote> stockQuotes) throws SerializationException {
            try {
                MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
                packer.packInt(stockQuotes.size());
                for(StockQuote stockQuote : stockQuotes) {
                    packer.packString(stockQuote.getTicker())
                    .packDouble(stockQuote.getPrice())
                    .packLong(stockQuote.getVolume())
                    .packLong(stockQuote.getTimestampMs());
                }
                packer.close();
                return packer.toByteArray();
            } catch (IOException e) {
                throw new SerializationException("Couldn't serialize stock quote", e);
            }
        }
    }

    public static class StockQuotesMessagePackDeserializer extends CacheDeserializer<LinkedList<StockQuote>> {

        @Override
        public LinkedList<StockQuote> deserialize(byte[] bytes) throws DeserializationException {
            try {
                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
                LinkedList<StockQuote> stockQuotes = new LinkedList<>();
                int numStockQuotes = unpacker.unpackInt();
                for(int i = 0; i < numStockQuotes; i++) {
                    String ticker = unpacker.unpackString();
                    double price = unpacker.unpackDouble();
                    long volume = unpacker.unpackLong();
                    long timestampMs = unpacker.unpackLong();
                    stockQuotes.add(new StockQuote(ticker, price, volume, timestampMs));
                }

                unpacker.close();
                return stockQuotes;
            } catch (IOException e) {
                throw new DeserializationException("Couldn't deserialize stock quote", e);
            }
        }
    }
}
