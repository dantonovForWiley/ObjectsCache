package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.impl.storage.FileSystemStorage;
import com.dantonov.wiley.objectscache.impl.storage.SerializationUtil;
import com.dantonov.wiley.objectscache.impl.storage.exceptions.*;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test {@link com.dantonov.wiley.objectscache.impl.storage.FileSystemStorage}
 */
public class FileSystemStorageTest {

    /**
     * Clean directory for cached objects before test
     */
    @BeforeTest
    public void prepareDirectory() {
        removeDirectory(TEST_DIRECTORY);
        try {
            fileSystemStorage = new FileSystemStorage(TEST_DIRECTORY);
        } catch (FileSystemBadDirectoryException e) {
            Assert.assertNull(e, "FileSystemBadDirectoryException exception is not expected on " +
                    "initialization");
        }
    }

    /**
     * Clean directory for cached object after test
     */
    @AfterTest
    public void clearDirectory() {
        removeDirectory(TEST_DIRECTORY);
    }

    /**
     * Test storing objects in {@link FileSystemStorage}
     */
    @Test
    public void storeObjects() {
        testObjectMap.entrySet().forEach((entry) -> {
            byte[] bytes = null;
            try {
                bytes = SerializationUtil.serializeObject(entry.getValue());
            } catch (IOException e) {
                Assert.assertNull(e, "Serrialization exception is not expected during storing " +
                        "objects");
            }
            try {
                fileSystemStorage.store(entry.getKey(), bytes);
            } catch (FailedToStoreDataInStorage failedToStoreDataInStorage) {
                Assert.assertNull(failedToStoreDataInStorage, "FailedToStoreDataInStorage  " +
                        "exception is not expected during storing objects");
            }
        });
    }

    /**
     * Test stored objects. Retrieve object and compare with cached value
     */
    @Test(dependsOnMethods = "storeObjects")
    public void checkStoredObjects() {
        testObjectMap.entrySet().forEach((entry) -> {
            try {
                byte[] bytes = fileSystemStorage.retrieve(entry.getKey());
                Object object = SerializationUtil.deserializeObject(bytes);
                Assert.assertEquals(object, entry.getValue(), "Retrieved object and stored object " +
                        "must be equals");
            } catch (FailedToRetrieveStorageData | DataNotFoundInStorage | IOException |
                    ClassNotFoundException e) {
                Assert.assertNull(e, e.getClass().getName() +
                        " exception is not expected on checking stored objects");
            }
        });
    }

    /**
     * Check deletion of cached objects from {@link FileSystemStorage}
     * <b>NOTE: test checks file on file system. this depends on how {@link FileSystemStorage}
     * saves files</b>
     */
    @Test(dependsOnMethods = "checkStoredObjects")
    public void checkDeleteFromStorage() {
        testObjectMap.entrySet().forEach((entry) -> {

            UUID uuid = entry.getKey();

            try {
                fileSystemStorage.delete(uuid);
            } catch (DataNotFoundInStorage | FailedToDeleteDataInStorage e) {
                Assert.assertNull(e, e.getClass().getName() +
                        " exception is not expected on deleting stored objects");
            }

            Assert.assertFalse(Files.exists(Paths.get(TEST_DIRECTORY, uuid.toString())), "File " +
                    "must not exist on file system after removing cached object from cache");

        });
    }

    /**
     * Utils function to clear directory
     *
     * @param dir directory to delete
     */
    public static void removeDirectory(String dir) {
        if (Files.exists(Paths.get(dir))) {
            try {
                Files.list(Paths.get(dir)).forEach((file) -> {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        Assert.assertNull(e, "Exception on file remove is unexpected");
                    }
                });
                Files.deleteIfExists(Paths.get(dir));
            } catch (Exception e) {
                Assert.assertNull(e, "Exception on directory remove is unexpected");
            }
        }
    }

    private Map<UUID, Object> testObjectMap = new HashMap<UUID, Object>() {{
        put(UUID.randomUUID(), new Integer(28));
        put(UUID.randomUUID(), "test string");
        put(UUID.randomUUID(), new TestSerializableObject(10, "10", false));
        put(UUID.randomUUID(), new TestSerializableObject(20, "20", true));
    }};

    private FileSystemStorage fileSystemStorage;

    private static final String TEST_DIRECTORY = "./target/file_system_storage";
}

