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
import com.scaleout.client.caching.query.CacheObjectAttribute;
import com.scaleout.client.caching.query.FilterFactory;

import java.io.Serializable;

public class QuerySample {
    public static class SampleObject implements Serializable {
        private int _sampleNum;
        private double _sampleDouble;
        private int _ignoredNum;
        public SampleObject(int num, double dub) {
            _sampleNum 		= num;
            _sampleDouble 	= dub;
        }

        @CacheObjectAttribute
        public int getSampleNum() {
            return _sampleNum;
        }

        @CacheObjectAttribute
        public double getSampleDouble() {
            return _sampleDouble;
        }

        public int getIgnoredNum() {
            return _ignoredNum;
        }
    }

    public static void main(String[] args) throws Exception {
        GridConnection connection = GridConnection.connect("bootstrapGateways=localhost:721");
        Cache<String, SampleObject> cache = new CacheBuilder<String, SampleObject>(connection, "example", String.class)
                .build();
        String key = "foo";
        SampleObject value = new SampleObject(23, 23.29);
        CacheResponse<String,SampleObject> response = cache.add(key, value);
        if(response.getStatus() != RequestStatus.ObjectAdded)
            System.out.println("Unexpected request status " + response.getStatus());
        // query the cache, looking for objects where the value for the property "getSampleNum" is less than 25
        Iterable<SampleObject> queryResponse = cache.queryObjects(FilterFactory.lessThan("getSampleNum", 25), SampleObject.class);
        for(SampleObject obj : queryResponse) {
            System.out.println("Queried object: " + obj);
        }
    }
}
