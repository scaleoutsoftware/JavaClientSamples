/*
 * (C) Copyright 2022 by ScaleOut Software, Inc.
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
package com.scaleout.caching.sample;

import com.scaleout.client.GridConnectException;
import com.scaleout.client.GridConnection;
import com.scaleout.client.ServiceEvents;
import com.scaleout.client.ServiceEventsException;
import com.scaleout.client.caching.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class ExpirationListener {
    public static void main(String[] args) throws ServiceEventsException, IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        System.out.println("Connecting to store 1...");
        GridConnection store1Connection = null;
        // The GridConnection automatically retries connections if TCP fails. However, the network may be slow to come
        // online, so we retry extra times to make sure the connection is established.
        for(int retry = 0; retry < 10; retry++) {
            try {
                store1Connection = GridConnection.connect("bootstrapGateways=store1:2721");
            } catch(GridConnectException gce) {
                System.out.println(String.format("Couldn't connect to grid after %s attempts, retrying.", retry));
            }
        }
        if(store1Connection == null) throw new RuntimeException("Couldn't connect to grid server.");
        else {
            System.out.println("Connected to store1!");
        }

        Cache<String, String> store1Cache = new CacheBuilder<String, String>(store1Connection, "sample", String.class)
                .geoServerPushPolicy(GeoServerPushPolicy.AllowReplication)
                .objectTimeout(Duration.ofSeconds(15))
                .objectTimeoutType(TimeoutType.Absolute)
                .build();

        ServiceEvents.setExpirationHandler(store1Cache, new CacheEntryExpirationHandler<String, String>() {
            @Override
            public CacheEntryDisposition handleExpirationEvent(Cache<String, String> cache, String key) {
                CacheEntryDisposition disposition = CacheEntryDisposition.NotHandled;
                System.out.printf("Object (%s) expired\n", key);
                if(key.equals("MyFavoriteKey"))
                    disposition = CacheEntryDisposition.Save;
                else disposition = CacheEntryDisposition.Remove;
                latch.countDown();
                return disposition;
            }
        });
        latch.await();
    }
}
