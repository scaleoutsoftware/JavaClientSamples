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
