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
package com.scaleout.client.samples.caching;

import com.scaleout.client.GridConnection;
import com.scaleout.client.caching.Cache;
import com.scaleout.client.caching.CacheBuilder;
import com.scaleout.client.caching.CacheResponse;
import com.scaleout.client.caching.RequestStatus;

public class LockingSample {
    public static void main(String[] args) throws Exception {
        GridConnection connection = GridConnection.connect("bootstrapGateways=server1:721,server2:721;");
        Cache<Integer, String> cache = new CacheBuilder<Integer, String>(connection, "example", Integer.class)
                .build();
        try {
            // create the object
            CacheResponse<Integer, String> response = cache.add(6, "John Adams");
            if(response.getStatus() != RequestStatus.ObjectAdded)
                System.out.println("Unexpected request status " + response.getStatus());
            // read the object
            response = cache.readAndLock(6);
            if (response.getStatus() == RequestStatus.ObjectRetrieved)
                System.out.println("6th US president: " + response.getValue());
            else
                System.out.println("Unexpected request status " + response.getStatus());
            // update the object, supplying the lock token from the read-and-lock request
            response = cache.updateAndUnlock(6, "John *Quincy* Adams", response.getLockToken());
            if (response.getStatus() != RequestStatus.ObjectUpdated)
                System.out.println("Unexpected request status " + response.getStatus());
            // delete the object
            response = cache.remove(6);
            if (response.getStatus() != RequestStatus.ObjectRemoved)
                System.out.println("Unexpected request status " + response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
