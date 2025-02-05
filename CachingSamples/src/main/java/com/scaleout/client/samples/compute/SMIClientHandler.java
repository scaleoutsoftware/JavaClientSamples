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
import com.scaleout.client.ServiceEvents;
import com.scaleout.client.ServiceEventsException;
import com.scaleout.client.caching.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;

public class SMIClientHandler {
    public static void main(String[] args) throws GridConnectException, CacheException, ServiceEventsException, IOException {
        // Connect to the cache that stores login times.
        GridConnection connection = GridConnection.connect("bootstrapGateways=localhost");
        Cache<String, PriceHistory> cache = new CacheBuilder<String, PriceHistory>(connection, "SMISample", String.class)
                .build();
        
        // Assign the cache entry method invocation handler, with a param holder
        ServiceEvents.setCacheEntryMethodInvocationHandler(cache, "Historic price query", new CacheEntryMethodInvocationHandler<String, PriceHistory, Long, Double>() {
            @Override
            public Double evaluate(String key, OperationContext<String, PriceHistory, Long> operationContext) {
                long timeToEvaluateMs = operationContext.getParameterObject();
                try {
                    CacheResponse<String,PriceHistory> readResponse = operationContext.getCache().read(key);
                    if(readResponse.getStatus() == RequestStatus.ObjectRetrieved) {
                        PriceHistory historyForKey = readResponse.getValue();
                        HashMap<Long,Double> dayToPriceMap = historyForKey.getPriceHistory();
                        return dayToPriceMap.getOrDefault(key, 0.0d);
                    } else {
                        // object not found
                        return 0.0d;
                    }
                } catch (CacheException e) {
                    throw new IllegalStateException("Handler threw an exception", e);
                }
            }

            @Override
            public byte[] serializeResult(Double aDouble) {
                return ByteBuffer.allocate(Double.BYTES).putDouble(aDouble).array();
            }
        }, new ParamHolder<Long>() {
            @Override
            public Long getParam(byte[] bytes) {
                return ByteBuffer.wrap(bytes).getLong();
            }
        });

        System.out.println("Waiting for events...");
        System.out.println("Press any key to exit.");
        System.in.read();
    }
}
