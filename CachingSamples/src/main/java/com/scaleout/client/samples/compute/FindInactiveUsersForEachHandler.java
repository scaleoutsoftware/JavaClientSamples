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

import com.scaleout.client.caching.CacheEntryForEachHandler;
import com.scaleout.client.caching.OperationContext;
import com.scaleout.client.caching.RequestStatus;

import java.time.Duration;

public class FindInactiveUsersForEachHandler implements CacheEntryForEachHandler<String, Long, Void> {
    @Override
    public void evaluate(String key, OperationContext<String, Long, Void> operationContext) {
        try {
            // Use the cache in the PMI context to retrieve the object being evaluated:
            var readResponse = operationContext.getCache().read(key);
            if (readResponse.getStatus() == RequestStatus.ObjectNotFound)
            {
                System.out.println(key + " removed by another client during PMI operation.");
                return;
            }
            else if (readResponse.getStatus() != RequestStatus.ObjectRetrieved)
            {
                System.out.println("Unexpected error (" + readResponse.getStatus() + ") while reading key: " + key);
                return;
            }

            // Perform analysis:
            long lastLoginEpochMs = readResponse.getValue();
            long inactiveTimeMs = System.currentTimeMillis() - lastLoginEpochMs;
            if (inactiveTimeMs > Duration.ofDays(7).toMillis())
            {
                System.out.println(key + " inactive for " + Duration.ofMillis(inactiveTimeMs).toDays() + " days.");
            }
        } catch (Exception e) {
            System.out.println("Unexpected error while processing PMI events: ");
            e.printStackTrace();
        }
    }
}
