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
package com.scaleout.client.samples.events;

import com.scaleout.client.GridConnection;
import com.scaleout.client.ServiceEvents;
import com.scaleout.client.caching.*;

import java.time.Duration;

public class WriteBehindSample {
    static class PlayerStats{
        public int wins;
        public int losses;
    }
    public static void main(String[] args) throws Exception {
        GridConnection connection = GridConnection.connect("bootstrapGateways=localhost:721");
        Cache<String, PlayerStats> cache = new CacheBuilder<String, PlayerStats>(connection, "example", String.class)
                .backingStoreEventInterval(Duration.ofSeconds(5))
                .backingStoreMode(BackingStoreMode.WriteBehind)
                .build();

        // Inform the ScaleOut service that this client will be processing write-behind
        // events by providing a lambda callback for the cache:
        ServiceEvents.setStoreObjectHandler(cache, (playerId, playerStats) -> {
            int playerWins = playerStats.wins;
            int playerLosses = playerStats.losses;
            // (...remainder of DB code elided.)

            System.out.println("Write behind.");
        });

        // Also register a handler for erase-behind events to mark a player as 'offline'
        // in the database upon removal from the ScaleOut cache:
        ServiceEvents.setEraseObjectHandler(cache, (playerId) -> {
            // UPDATE players SET status = "offline" where player_id = ...
            System.out.println("Erase behind.");
        });

        System.out.println("Waiting for events...");
        System.out.println("Press any key to exit.");
        System.in.read();
    }
}

