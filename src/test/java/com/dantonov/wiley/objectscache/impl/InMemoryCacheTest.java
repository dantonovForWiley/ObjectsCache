package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.Cache;
import com.dantonov.wiley.objectscache.ConfigurationValue;
import com.dantonov.wiley.objectscache.exceptions.AllocationInCacheException;
import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.testng.Assert.*;

/**
 * Test {@link InMemoryCache}
 */
public class InMemoryCacheTest {

    @BeforeTest
    public void initInMemoryCache() throws UnacceptableCacheBuildParameter {
        inMemoryCache = new InMemoryCache.Builder().setMaxCacheSize(MAX_CACHE_SIZE).setDangerLoad
                (DANGER_LOAD).build();
    }

    /**
     * The goal is to verify configuration values responses during adding objects to the
     * {@link InMemoryCache}.<br>
     */
    @Test
    public void exceedMaxCacheValue() {

        cachedObjects = new HashMap<>();

        IntStream.rangeClosed(1, MAX_CACHE_SIZE + 2).forEach(i -> {
            Integer originalObjectToCache = Integer.valueOf(i);
            ObjectInCache objectInCache = new ObjectInCache.Builder().setObject(originalObjectToCache).build();
            try {
                inMemoryCache.allocateObject(objectInCache);
            } catch (AllocationInCacheException e) {
                assertNull(e, "Allocation exception is not expected");
            }

            cachedObjects.put(objectInCache, originalObjectToCache);

            long notRespondingValuesCount = inMemoryCache.getConfigurationValues()
                    .stream().filter
                            (ConfigurationValue::isResponding)
                    .count();

            if (i <= DANGER_LOAD) {
                assertEquals(notRespondingValuesCount, 2L, String.format("When " +
                        "inMemoryCache has %s values, there should be two responding " +
                        "configuration values", i));
            } else if (i > DANGER_LOAD && i <= MAX_CACHE_SIZE) {
                assertEquals(notRespondingValuesCount, 1L, String.format("When " +
                        "inMemoryCache has %s values, there should be one responding " +
                        "configuration value", i));
            } else {
                assertEquals(notRespondingValuesCount, 0L, String.format("When " +
                        "inMemoryCache has %s values, there should not be responding " +
                        "configuration values", i));
            }
        });
    }

    /**
     * The goal is to verify cached objects
     */
    @Test(dependsOnMethods = "exceedMaxCacheValue")
    public void checkCachedObjects() {
        cachedObjects.entrySet().forEach((entry) -> {
            ObjectInCache referenceToCachedObject = entry.getKey();
            Object originalObject = entry.getValue();
            assertTrue(inMemoryCache.contains(referenceToCachedObject), "Cache must " +
                    "respond containing for cached object reference");
            try {
                ObjectInCache newObjectInCacheRef = inMemoryCache.returnObject
                        (referenceToCachedObject);
                assertEquals(originalObject, newObjectInCacheRef.objectRef(), "ObjectInCache, " +
                        "referencing to object got from cache must contains the same object that " +
                        "had been cached");
            } catch (ObjectNotFoundInCache objectNotFoundInCache) {
                assertNull(objectNotFoundInCache, "ObjectNotFoundInCache exception on " +
                        "getting cached object from cache is not expected");
            }
        });
        assertEquals(getRespondingConfigurationValuesCount(), 0L, "All (actually 2) " +
                "configuration values must be not responding when all cached objects just " +
                "verified");
    }

    /**
     * The goal is to verify cache and configuration values during releasing objects from cache
     */
    @Test(dependsOnMethods = "checkCachedObjects")
    public void freeObjectsFromCache() {
        cachedObjects.entrySet().forEach((entry) -> {
            ObjectInCache referenceToCachedObject = entry.getKey();
            Object originalObject = entry.getValue();
            try {
                assertNull(referenceToCachedObject.objectRef(), "ObjectInCache, " +
                        "referencing to cached " +
                        "object " +
                        "must contain null object reference for this test. This means " +
                        "cache implementation has cleared reference after caching object.");
                ObjectInCache newObjectInCacheRef = inMemoryCache.freeObject(referenceToCachedObject);
                assertNotNull(newObjectInCacheRef.objectRef(), "ObjectInCache, referencing" +
                        " to object released by cache must contains reference to object. This " +
                        "means cache implementation has returned object back.");
                assertEquals(originalObject, newObjectInCacheRef.objectRef(), "ObjectInCache, " +
                        "referencing to object released by cache must contains the same object that had been " +
                        "cached");
            } catch (ObjectNotFoundInCache objectNotFoundInCache) {
                assertNull(objectNotFoundInCache, "ObjectNotFoundInCache exception on " +
                        "removing cached object from cache is not expected");
            }
        });
        assertEquals(getRespondingConfigurationValuesCount(), 2L, "All (actually 2) configuration " +
                "values must be responding when all cached objects released");
    }

    private long getRespondingConfigurationValuesCount() {
        return inMemoryCache.getConfigurationValues().stream().filter
                (ConfigurationValue::isResponding).count();
    }

    private Cache inMemoryCache;
    private final int MAX_CACHE_SIZE = 100;
    private final int DANGER_LOAD = 80;

    private Map<ObjectInCache, Object> cachedObjects;


}
