package com.scaleout.caching.sample;

import com.scaleout.client.GridConnectException;
import com.scaleout.client.GridConnection;
import com.scaleout.client.caching.*;

import java.time.Duration;

public class CacheRunner {
    public static void main(String[] args) throws CacheException {
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

        System.out.println("Connecting to store 2...");
        GridConnection store2Connection = null;
        for(int retry = 0; retry < 10; retry++) {
            try {
                store2Connection = GridConnection.connect("bootstrapGateways=store2:3721");
            } catch(GridConnectException gce) {
                System.out.println(String.format("Couldn't connect to grid after %s attempts, retrying.", retry));
            }
        }
        if(store2Connection == null) throw new RuntimeException("Couldn't connect to grid server.");
        else {
            System.out.println("Connected to store2!");
        }

        Cache<String, String> store1Cache = new CacheBuilder<String, String>(store1Connection, "sample", String.class)
                .geoServerPushPolicy(GeoServerPushPolicy.AllowReplication)
                .objectTimeout(Duration.ofSeconds(15))
                .objectTimeoutType(TimeoutType.Absolute)
                .build();

        Cache<String, String> store2Cache = new CacheBuilder<String, String>(store2Connection, "sample", String.class)
                .build();

        System.out.println("Adding object to cache in store 1!");
        CacheResponse<String, String> addResponse = store1Cache.add("MyKey", "MyValue");
        System.out.println("Object " + ((addResponse.getStatus() == RequestStatus.ObjectAdded ? "added" : "not added.")) + " to cache in store 1.");

        addResponse = store1Cache.add("MyFavoriteKey", "MyFavoriteValue");
        System.out.println("Object " + ((addResponse.getStatus() == RequestStatus.ObjectAdded ? "added" : "not added.")) + " to cache in store 1.");

        System.out.println("Reading object from cache in store 2!");
        CacheResponse<String,String> readResponse = store2Cache.read("foo");
        System.out.println("Object " + ((readResponse.getStatus() == RequestStatus.ObjectRetrieved ? "retrieved" : "not retrieved.")) + " from cache in store 2.");
    }
}
