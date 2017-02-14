package com.dantonov.wiley.objectscache;

import com.dantonov.wiley.objectscache.exceptions.AllocationException;
import com.dantonov.wiley.objectscache.exceptions.AllocationInCacheException;
import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;
import com.dantonov.wiley.objectscache.exceptions.UnacceptableCacheBuildParameter;
import com.dantonov.wiley.objectscache.impl.*;
import com.dantonov.wiley.objectscache.impl.storage.FileSystemStorage;
import com.dantonov.wiley.objectscache.impl.storage.SerializableStorage;
import com.dantonov.wiley.objectscache.impl.storage.exceptions.FileSystemBadDirectoryException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>Test to implement and verify the following cache:</b><br>
 * A configurable two-level cache (for caching Objects).<br>
 * Level 1 is memory, level 2 is filesystem.<br>
 * Config params should let one specify the cache strategies and max sizes of level 1 and 2<br>
 * <br>
 * <ul><b>Limitations:</b>
 * <li>File system cache can store only serializable objects</li>
 * <li>Only one strategy is implemented for storing objects: {@link ObjectCacheStrategyImpl}</li>
 * </ul>
 */
public class GeneralPresentationTest {

    @BeforeTest
    public void initializeCache() {

        // Step 1. Configure in-memory cache
        try {
            inMemoryCache = new InMemoryCache.Builder()
                    .setMaxCacheSize(IN_MEMORY_CACHE_SIZE).setDangerLoad(inMemoryDangerLoadLevel).build();
        } catch (UnacceptableCacheBuildParameter unacceptableCacheBuildParameter) {
            Assert.assertNull(unacceptableCacheBuildParameter, "In memory cache building " +
                    "exception is not expected");
        }

        // Step 2. Configure filesystem cache
        // 2.1 configure storage for objects that will save data to the files
        try {
            serializableStorage = new FileSystemStorage
                    (directoryForFileSystemCache);
        } catch (FileSystemBadDirectoryException e) {
            Assert.assertNull(e, "FileSystemBadDirectoryException during creating file system " +
                    "storage is not expected");
        }
        // 2.2 configure second level cache as a serializable storage cache with underlying file
        // storage
        filesystemCache = new SerializableStorageCache(serializableStorage, FILE_SYSTEM_CACHE_SIZE);

        // Step 3.
        // 3.1 Set cache hierarchy - in-memory cache is a first level, filesystem cache - second
        // level
        cacheHierarchy = new SimpleChacheListHierarchy.Builder().addCache(inMemoryCache).addCache
                (filesystemCache).build();
        // 3.2 select caching strategy. only one is available for now
        objectCacheStrategy = new ObjectCacheStrategyImpl();
        // 3.3 build objects cache. set all configured instances as parameters
        objectsCache = new ObjectCacheImpl.Builder().setCacheMovingStrategy
                (objectCacheStrategy).setCacheLevelModel(cacheHierarchy).build();
    }

    /**
     * One-function test with comments to illustrate cache interaction
     */
    @Test
    public void demonstrateCacheFunctionality() {

        Map<CachedObject, String> cached11strings = new HashMap<>();

        // lets put 11 string to cache
        for (String stringToCache : stringToCacheWith11Elements) {
            // cachedStringReference will referece to cached object
            CachedObject cachedStringReference = null;
            try {
                cachedStringReference = objectsCache.cacheObject(stringToCache);
            } catch (AllocationException e) {
                Assert.assertNull(e, "AllocationException on adding to cache is not expected");
            }
            // remember reference
            cached11strings.put(cachedStringReference, stringToCache);
        }

        // according to strategy, now, one string must be saved on file system
        // if you put break point here and verify folder - you will see one file

        long objectCountInMemoryCache = cached11strings.keySet().stream().filter((cachedObject -> {
            // trick. we creates internal object that is acceptable by cache to search in cache
            ObjectInCache internalReference = ObjectInCache.from(cachedObject.getUuid());
            return inMemoryCache.contains(internalReference);
        })).count();

        Assert.assertEquals(objectCountInMemoryCache, 8L, "In-memory cache should keep 8 " +
                "objects when we have added 11");

        // NOTE!!! max cache size is 10, BUT danger load is 80 percents. So 8 elements is stored.
        // if you set inMemoryDangerLoadLevel variable to 100 - here we will se 10 elements

        long objectCountInFileSystemCache = cached11strings.keySet().stream().filter((cachedObject
                -> {
            // trick. we creates internal object that is acceptable by cache to search in cache
            ObjectInCache internalReference = ObjectInCache.from(cachedObject.getUuid());
            return filesystemCache.contains(internalReference);
        })).count();

        // now lets verify file system cache
        Assert.assertEquals(objectCountInFileSystemCache, stringToCacheWith11Elements.size()
                - objectCountInMemoryCache, "File system cache must store other elements that is " +
                "not stored in in-memory cache");

        // now, lets add a new object to cache
        CachedObject cachedException = null;
        try {
            cachedException = objectsCache.cacheObject(exceptionToCache);
        } catch (AllocationException e) {
            Assert.assertNull(e, "AllocationException on caching our exception object is not " +
                    "expected");
        }

        // lets verify where is out exception is stored
        ObjectInCache internalObjectInCacheForException = ObjectInCache.from(cachedException
                .getUuid());

        // exception is stored in in-memory cache
        Assert.assertTrue(inMemoryCache.contains(internalObjectInCacheForException), "In-memory " +
                "cache should contain stored exception");

        // verify exception is not stored in file system cache
        Assert.assertFalse(filesystemCache.contains(internalObjectInCacheForException),
                "Filesystem cache should not contain stored exception");

        // lest get our stored exception
        Object objFromCache = null;
        try {
            objFromCache = cachedException.getObject();
        } catch (ObjectNotFoundInCache | AllocationInCacheException e) {
            Assert.assertNull(e, e.getClass() + " exception is not expected on retrieving cached " +
                    "exception");
        }
        Assert.assertEquals(((IOException) objFromCache).getMessage(), exceptionMessage,
                String.format("Retrieved" +
                        " object must be an IOException with %s message", exceptionMessage));

        // lets remove all stored strings
        cached11strings.entrySet().forEach((entry) -> {

            CachedObject ref = entry.getKey();
            String originalString = entry.getValue();
            String retrievedString = null;
            try {
                Object obj = ref.getObject();
                retrievedString = (String) obj;
            } catch (ObjectNotFoundInCache | AllocationInCacheException | ClassCastException e) {
                Assert.assertNull(e, e.getClass() + " exception is not expected on retrieving cached " +
                        "string");
            }

            Assert.assertEquals(retrievedString, originalString, "Retrieved string must equals " +
                    "original one");

            // remove object from cache
            objectsCache.releaseCachedObject(ref);

            try {
                ref.getObject();
            } catch (ObjectNotFoundInCache e) {
                Assert.assertNotNull(e, e.getClass() + " exception is expected after deletion " +
                        "of cached string");
            } catch (AllocationInCacheException e2) {
                Assert.assertNull(e2, e2.getClass() + " exception is expected after deletion " +
                        "of cached string");
            }
        });

        // if you put breakpoint here and look on directory - where will not be files
        long occupiedBytesByFilesystemCache = serializableStorage.getOccupiedSize();
        Assert.assertEquals(occupiedBytesByFilesystemCache, 0L, "File system should not occupy " +
                "disk space when all objects are deleted");

    }

    @AfterTest
    public void clearDir() {
        FileSystemStorageTest.removeDirectory(directoryForFileSystemCache);
    }

    private Cache inMemoryCache;
    // 80% is a danger load level for in-memory cache
    private int inMemoryDangerLoadLevel = 80;


    private Cache filesystemCache;
    private String directoryForFileSystemCache = "./target/files_cache/";
    private SerializableStorage serializableStorage;

    private CacheHierarchy cacheHierarchy;
    private ObjectCacheStrategy objectCacheStrategy;

    // Objects cache - top level entity to cache objects
    private ObjectsCache objectsCache;


    private List<String> stringToCacheWith11Elements = Arrays.asList("A1", "B2", "C3", "D4", "E5",
            "F6", "G7", "H8", "I9", "G10", "K11");

    private String exceptionMessage = "hello";
    private IOException exceptionToCache = new IOException(exceptionMessage);

    /**
     * Configurable value.<br>
     * Size for in-memory cache. max objects count.
     */
    public static final Integer IN_MEMORY_CACHE_SIZE = 10;
    /**
     * Configurable value<br>
     * Size for file system cache. occupied size in bytes
     */
    public static final Long FILE_SYSTEM_CACHE_SIZE = 100L;

}
