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
import com.scaleout.client.caching.Cache;
import com.scaleout.client.caching.CacheBuilder;
import com.scaleout.client.caching.CacheException;
import com.scaleout.client.caching.InvokeResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedList;

public class PostEventClientSample {
    public static void main(String[] args) throws GridConnectException, IOException, CacheException {
        GridConnection connection = GridConnection.connect("bootstrapGateways=localhost");
        Cache<String, LinkedList<StockQuote>> cache = new CacheBuilder<String, LinkedList<StockQuote>>(connection, "PostEventSample", String.class)
                .customSerialization(new StockQuoteSerializers.StockQuotesMessagePackSerializer(), new StockQuoteSerializers.StockQuotesMessagePackDeserializer())
                .build();

        // Simulate an incoming stock quote:
        var quote = new StockQuote("MSFT",126.90, 14330868, System.currentTimeMillis());

        // Post the quote to the ScaleOut service holding this stock's history.
        // Here, ticker symbols are the keys to history objects in the cache.
        // Use it as the key for the post operation to cause the event to be
        // raised on the ScaleOut where the MSFT history resides
        InvokeResponse response = cache.postEvent(quote.getTicker(), "stockQuote", StockQuote.toBytes(quote), Duration.ofSeconds(2));
        switch (response.getRequestStatus()) {
            case EventPosted:
                System.out.println("Event was successfully posted.");
                break;
            case UnhandledExceptionInCallback:
                System.out.println("Unhandled exception thrown from Posted Event Handler.");
                System.out.println(new String(response.getErrorData(), StandardCharsets.UTF_8));
                break;
        }
    }
}
