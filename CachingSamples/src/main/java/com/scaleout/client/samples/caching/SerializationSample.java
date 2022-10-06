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
package com.scaleout.client.samples.caching;

import com.google.protobuf.InvalidProtocolBufferException;
import com.scaleout.client.GridConnection;
import com.scaleout.client.caching.*;
import com.scaleout.client.samples.caching.protos.Person;


public class SerializationSample {
    /**
     * This class turns a Protobuf "Person" into abyte array
     */
    static class ProtobufSerializer extends CacheSerializer<Person> {
        @Override
        public byte[] serialize(Person person) throws SerializationException {
            return person.toByteArray();
        }
    }

    /**
     * This class deserializes a Person protobuf message.
     */
    static class ProtobufDeserializer extends CacheDeserializer<Person> {
        @Override
        public Person deserialize(byte[] bytes) throws DeserializationException {
            try {
                return Person.parseFrom(bytes);
            } catch (InvalidProtocolBufferException e) {
                throw new DeserializationException(e.getMessage(), e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        GridConnection connection = GridConnection.connect("bootstrapGateways=localhost:721;");
        Cache<String, Person> cache = new CacheBuilder<String, Person>(connection, "example", String.class)
                // supply the custom serializer to the builder
                .customSerialization(new ProtobufSerializer(), new ProtobufDeserializer())
                .build();
        Person john =
                Person.newBuilder()
                        .setId(1234)
                        .setName("John Doe")
                        .setEmail("jdoe@example.com")
                        .addPhones(
                                Person.PhoneNumber.newBuilder()
                                        .setNumber("555-4321")
                                        .setType(Person.PhoneType.HOME))
                        .build();

        CacheResponse<String, Person> response = cache.add("john", john);
        if(response.getStatus() == RequestStatus.ObjectAdded) {
            System.out.println("Object serialized with Protobuf and added to cache.");
        } else {
            System.out.println("Unexpected request status: " + response.getStatus());
        }

        response = cache.read("john");
        if(response.getStatus() == RequestStatus.ObjectRetrieved) {
            System.out.println("Object retrieved from cache and deserialized with Protobuf.");
        } else {
            System.out.println("Unexpected request status: " + response.getStatus());
        }
    }
}
