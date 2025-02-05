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

import com.scaleout.client.GridConnectException;
import com.scaleout.client.GridConnection;
import com.scaleout.client.ServiceEvents;
import com.scaleout.client.ServiceEventsException;
import com.scaleout.client.caching.*;

import java.io.IOException;
import java.util.LinkedList;

public class EventListenerSample {
    public static void main(String[] args) throws GridConnectException, ServiceEventsException, IOException {
        GridConnection connection = GridConnection.connect("bootstrapGateways=localhost");
        Cache<String, LinkedList<StockQuote>> cache = new CacheBuilder<String, LinkedList<StockQuote>>(connection, "PostEventSample", String.class)
                .customSerialization(new StockQuoteSerializers.StockQuotesMessagePackSerializer(), new StockQuoteSerializers.StockQuotesMessagePackDeserializer())
                .build();

        ServiceEvents.setPostedEventHandler(cache, (s, eventPayload) -> {
            try {
                // Extract stock quote from event payload.
                StockQuote quote = StockQuote.fromBytes(eventPayload.getPayload());
                // Retrieve (or add, if it doesn't exit) the price history
                // from the local ScaleOut service.
                var response = cache.readOrAdd(
                        quote.getTicker(),
                        new ValueFactory<String, LinkedList<StockQuote>>() {
                            @Override
                            public ValueFactoryResult<LinkedList<StockQuote>> create(String s) {
                                LinkedList<StockQuote> value = new LinkedList<>();
                                value.add(quote);
                                ValueFactoryResult<LinkedList<StockQuote>> vfr = new ValueFactoryResult<LinkedList<StockQuote>>(value);
                                return vfr;
                            }
                        },
                        ReadPolicyBuilder.defaultPolicy());


                switch (response.getStatus())
                {
                    case ObjectRetrieved:
                        LinkedList<StockQuote> history = response.getValue();

                        // Add the incoming quote to the history:
                        history.addLast(quote);

                        // Trim the history to 100 of the most recent quotes:
                        if (history.size() > 100) history.removeFirst();

                        // Persist updated history to ScaleOut service:
                        cache.update(quote.getTicker(), history);
                        break;
                    case ObjectAdded:
                        // This was the first quote for the symbol. The valueFactory
                        // argument used in the ReadOrAddAsync call above already added a
                        // new history object in the service with the quote, so there's
                        // nothing to do.
                        break;
                    default:
                        System.out.println("Unexpected response: " + response.getStatus());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (CacheException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Waiting for events...");
        System.out.println("Press any key to exit.");
        System.in.read();
    }
}
