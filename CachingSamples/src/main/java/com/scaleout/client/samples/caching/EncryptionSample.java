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
import com.scaleout.client.caching.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public class EncryptionSample {
    // this is a sample 32-byte key, *** do not use this ***
    static final byte[] SECRET_KEY_VALUE = new byte[] {
            'T', 'H', 'I', 'S', 'I', 'S', 'N', 'O', 'T', 'A',
            'S', 'E', 'C', 'U', 'R', 'E', 'K', 'E', 'Y', '!',
            '!', '!', '!', '!', '!', '!', '!', '!', '!', '!', '!', '!'};

    static final String AES_ALGORITHM = "AES";

    static class SimpleObject {
        String simpleString;
        int simpleInt;
        long simpleLong;
        public SimpleObject(String str, int i, long l) {
            simpleString = str;
            simpleInt = i;
            simpleLong = l;
        }

        @Override
        public String toString() {
            return "SimpleObject{" +
                    "simpleString='" + simpleString + '\'' +
                    ", simpleInt=" + simpleInt +
                    ", simpleLong=" + simpleLong +
                    '}';
        }
    }

    /**
     * This class turns a SimpleObject into an AES-256 bit encrypted byte array
     */
    static class SimpleObjectEncryptingSerializer extends CacheSerializer<SimpleObject> {
        @Override
        public byte[] serialize(SimpleObject simpleObject) throws SerializationException {
            byte[] strAsBytes = simpleObject.simpleString.getBytes(StandardCharsets.UTF_8);
            byte[] intAsBytes = ByteBuffer.allocate(4).putInt(simpleObject.simpleInt).array();
            byte[] longAsBytes = ByteBuffer.allocate(8).putLong(simpleObject.simpleLong).array();
            byte[] strLengthAsBytes = ByteBuffer.allocate(4).putInt(strAsBytes.length).array();
            byte[] simpleObjAsBytes = ByteBuffer.allocate(strAsBytes.length + intAsBytes.length + longAsBytes.length + strLengthAsBytes.length)
                    .put(strLengthAsBytes)
                    .put(strAsBytes)
                    .put(intAsBytes)
                    .put(longAsBytes)
                    .array();
            // encrypt the byte[]
            try {
                Key key = new SecretKeySpec(SECRET_KEY_VALUE, AES_ALGORITHM);
                Cipher c = Cipher.getInstance(AES_ALGORITHM);
                c.init(Cipher.ENCRYPT_MODE, key);
                return c.doFinal(simpleObjAsBytes);
            } catch (Exception e) {
                throw new SerializationException(e.getMessage());
            }
        }
    }

    /**
     * This class decrypts an AES-256 bit encrypted byte array into a SimpleObject
     */
    static class SimpleObjectDecryptingDeserializer extends CacheDeserializer<SimpleObject> {
        @Override
        public SimpleObject deserialize(byte[] bytes) throws DeserializationException {
            try {
                // decrypt the byte[]
                Key key = new SecretKeySpec(SECRET_KEY_VALUE, AES_ALGORITHM);
                Cipher c = Cipher.getInstance(AES_ALGORITHM);
                c.init(Cipher.DECRYPT_MODE, key);

                // read each value from the decrypted byte[]
                String str = null;
                int i = 0;
                long l = 0L;
                ByteBuffer buffer = ByteBuffer.wrap(c.doFinal(bytes));
                int strLen = buffer.getInt();
                byte[] strAsBytes = new byte[strLen];
                buffer.get(strAsBytes);
                str = new String(strAsBytes, StandardCharsets.UTF_8);
                i = buffer.getInt();
                l = buffer.getLong();
                return new SimpleObject(str, i, l);
            } catch (Exception e) {
                throw new DeserializationException(e.getMessage());
            }

        }
    }

    public static void main(String[] args) throws Exception {
        GridConnection connection = GridConnection.connect("bootstrapGateways=server1:721,server2:721;");
        Cache<String, SimpleObject> cache = new CacheBuilder<String, SimpleObject>(connection, "example", String.class)
                // supply the custom serializer to the builder
                .customSerialization(new SimpleObjectEncryptingSerializer(), new SimpleObjectDecryptingDeserializer())
                .build();
        SimpleObject object = new SimpleObject("foo", 5, 23L);
        CacheResponse<String, SimpleObject> response = cache.add("my_simple_obj_key", object);
        if(response.getStatus() == RequestStatus.ObjectAdded) {
            System.out.println("Object encrypted with AES-256 bit encryption and added to cache.");
        } else {
            System.out.println("Unexpected request status: " + response.getStatus());
        }

        response = cache.read("my_simple_obj_key");
        if(response.getStatus() == RequestStatus.ObjectRetrieved) {
            System.out.println("Object retrieved from cache and decrypted.");
        } else {
            System.out.println("Unexpected request status: " + response.getStatus());
        }
    }
}
