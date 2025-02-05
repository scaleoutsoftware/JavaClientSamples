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
package com.scaleout.client.samples.compute;

import com.scaleout.client.GridConnectException;
import com.scaleout.client.GridConnection;
import com.scaleout.client.caching.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Random;

public class SMIClientSample {

    public static long addLargeHistoryObject() throws GridConnectException, CacheException {
        // Return a random time to evaluate in the invoke call...
        long timeToEvaluate = 0L;
        // Create a large object with 10 years of price history.
        int historyDays = 3650;

        // Add fake data to ClosingPrices:
        Random rand = new Random();
        long twentyFourHoursMs = 1000 * 60 * 60 * 24;
        long currentTimeMs = System.currentTimeMillis();
        long midnightOffset = currentTimeMs % twentyFourHoursMs;
        long historicDate = currentTimeMs - midnightOffset;

        String stockTicker = "MSFT";
        HashMap<Long, Double> generatedHistory = new HashMap<>();
        int idxToEvaluate = rand.nextInt(historyDays);
        for (int i = 0; i < historyDays; i++) {
            historicDate = historicDate - twentyFourHoursMs;
            generatedHistory.put(historicDate, 200.0d + rand.nextDouble());
            timeToEvaluate = i == idxToEvaluate ? historicDate : 0L;
        }

        PriceHistory priceHistory = new PriceHistory(stockTicker, generatedHistory);

        // Connect to Scaleout service
        GridConnection connection = GridConnection.connect("bootstrapGateways=localhost");
        Cache<String, PriceHistory> cache = new CacheBuilder<String, PriceHistory>(connection, "SMISample", String.class)
                .build();


        // Send this large object to the ScaleOut service:
        CacheResponse<String,PriceHistory> addResponse = cache.addOrUpdate(priceHistory.getStockTicker(), priceHistory);

        if(addResponse.getStatus() != RequestStatus.ObjectAdded || addResponse.getStatus() != RequestStatus.ObjectUpdated) {
            throw new RuntimeException("Couldn't add object to ScaleOut cache.");
        }
        return timeToEvaluate;
    }

    public static void main(String[] args) throws GridConnectException, CacheException {
        // Connect to the cache that stores login times.
        GridConnection connection = GridConnection.connect("bootstrapGateways=localhost");
        Cache<String, PriceHistory> cache = new CacheBuilder<String, PriceHistory>(connection, "SMISample", String.class)
                .build();

        // Add some test data to the cache:
        long timeToEvaluate = addLargeHistoryObject();

        InvokeResponse response = cache.singleInvoke("MSFT", "Historic price query", ByteBuffer.allocate(Long.BYTES).putLong(timeToEvaluate).array(), Duration.ofSeconds(0));
        switch (response.getRequestStatus()) {
            case InvokeComplete:
                System.out.println("Invoke complete.");
                if(response.getSuccessCount() == 1) {
                    System.out.println(ByteBuffer.wrap(response.getResultObject()).getDouble());
                }
                break;
            case UnhandledExceptionInCallback:
                System.out.println("Unhandled exception thrown from PMI Handler.");
                System.out.println(new String(response.getErrorData(), StandardCharsets.UTF_8));
                break;
            default:
                System.out.println("Unexpected response: " + response.getRequestStatus());
        }
    }
}
