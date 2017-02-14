package com.dantonov.wiley.objectscache.impl.storage;

import java.io.*;

/**
 * Util class implements methods to serialize and deserialize objects
 */
public class SerializationUtil {

    /**
     * Method to serialize object
     *
     * @param object {@link Object} to serialize
     * @return <code>byte[]</code> sequence
     * @throws IOException in case if serialization fails
     */
    public static byte[] serializeObject(Object object) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(baos)) {
            objectStream.writeObject(object);
            return baos.toByteArray();
        }
    }

    /**
     * Method to deserialize <code>byte[]</code> sequence to object
     *
     * @param bytes <code>byte[]</code> sequence to deserialize
     * @return {@link Object}
     * @throws IOException            in case if serialization fails
     * @throws ClassNotFoundException in case if serialization fails
     */
    public static Object deserializeObject(byte[] bytes) throws IOException,
            ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }
}
