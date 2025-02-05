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
import com.scaleout.client.caching.Cache;
import com.scaleout.client.caching.CacheBuilder;
import com.scaleout.client.caching.CacheException;
import com.scaleout.client.caching.InvokeResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class PMIClientSample {
    public static void main(String[] args) throws GridConnectException, CacheException {
        // Connect to the cache that stores login times.
        GridConnection connection = GridConnection.connect("bootstrapGateways=localhost");
        Cache<String, Long> cache = new CacheBuilder<String, Long>(connection, "PMISample", String.class)
                .build();

        // Add some test data to the cache:
        for (int i = 0; i < 50; i++)
        {
            long lastLogin = System.currentTimeMillis() - Duration.ofDays(i % 10).toMillis();
            cache.addOrUpdate("User_"+i, lastLogin);
        }
        // ... continued below

        InvokeResponse response = cache.invoke("Find inactive users", Duration.ofSeconds(0));
        switch (response.getRequestStatus()) {
            case InvokeComplete:
                System.out.println("Invoke complete.");
                System.out.println(response.getSuccessCount() + " were successfully evaluated.");
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
