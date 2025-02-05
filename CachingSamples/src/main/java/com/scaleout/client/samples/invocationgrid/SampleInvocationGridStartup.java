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
package com.scaleout.client.samples.invocationgrid;

import com.scaleout.client.GridConnection;
import com.scaleout.client.ServiceEvents;
import com.scaleout.client.ServiceEventsException;
import com.scaleout.client.caching.*;
import com.scaleout.client.ighosting.InvocationGridStartup;
import org.apache.logging.log4j.Logger;

public class SampleInvocationGridStartup implements InvocationGridStartup {
    @Override
    public void configure(GridConnection gridConnection, Logger logger, byte[] startupParam, String gridName) {
        Cache<String,String> expiriationCache = new CacheBuilder<String,String>(gridConnection, "cache_with_expiration_policy",String.class).build();
        Cache<String,String> postEventCache = new CacheBuilder<String,String>(gridConnection, "post_event_cache",String.class).build();
        try {
            ServiceEvents.setExpirationHandler(expiriationCache, (cache, s) -> {
                logger.trace("Key " + s + " expired.");
                return CacheEntryDisposition.Remove;
            });
            ServiceEvents.setPostedEventHandler(postEventCache, (s, eventPayload) -> logger.info("message received for key: " + s + " event info: " + eventPayload.getEventInfo()));
        } catch (ServiceEventsException e) {
            throw new RuntimeException(e);
        }
    }
}
