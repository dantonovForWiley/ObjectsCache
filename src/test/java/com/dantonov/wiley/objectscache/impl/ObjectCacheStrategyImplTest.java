package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.*;
import com.dantonov.wiley.objectscache.exceptions.AllocationException;
import com.dantonov.wiley.objectscache.exceptions.UnacceptableCacheBuildParameter;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Test {@link ObjectCacheStrategyImpl} <br>
 * Simple test. configure two in-memery caches.<br>
 * Will add more that both caches 'can store' - exceed configuration restrictions.<br>
 * Top level cache must have all configuration values responsible.
 * Second level cache will keep all other objects and configuration values will not respond.
 */
public class ObjectCacheStrategyImplTest {

    @BeforeTest
    public void configureAll() {

        try {
            cache1 = new InMemoryCache.Builder()
                    .setMaxCacheSize(cache1Size).setDangerLoad(cache1DangerLevel).build();
        } catch (UnacceptableCacheBuildParameter unacceptableCacheBuildParameter) {
            Assert.assertNull(unacceptableCacheBuildParameter, "Cache1 building exception is not" +
                    " expected");
        }

        try {
            cache2 = new InMemoryCache
                    .Builder().setMaxCacheSize(cache2Size).setDangerLoad(cache2DangerLevel).build();
        } catch (UnacceptableCacheBuildParameter unacceptableCacheBuildParameter) {
            Assert.assertNull(unacceptableCacheBuildParameter, "Cache2 building exception is not" +
                    " expected");
        }

        cacheHierarchy = new SimpleChacheListHierarchy.Builder().addCache(cache1).addCache(cache2)
                .build();

        cacheStrategy = new ObjectCacheStrategyImpl();

        objectsCache = new ObjectCacheImpl.Builder().setCacheMovingStrategy
                (cacheStrategy).setCacheLevelModel(cacheHierarchy).build();

        listObjectsToCache = generateStrings(objectsToCache);
        cachedObjects = new HashMap<>();


    }

    @Test
    public void allocateAllInCache() {
        for (String stringToCache : listObjectsToCache) {
            try {
                cachedObjects.put(stringToCache, objectsCache.cacheObject(stringToCache));
            } catch (AllocationException e) {
                Assert.assertNull(e, "AllocationException exception is not expected on locating " +
                        "all object to cache");
            }
        }
    }

    @Test(dependsOnMethods = "allocateAllInCache")
    public void verifyCache1() {
        System.out.println("Verify cache1:");
        cache1.getConfigurationValues().forEach((configurationValue ->
                printCacheConfigurationValue(cache1, configurationValue)
        ));
        long respondingConfigValues = cache1.getConfigurationValues().stream().filter
                (ConfigurationValue::isResponding).count();

        Assert.assertEquals(respondingConfigValues, 2L, "All configuration values in cache 1 must" +
                " respond");
    }

    @Test(dependsOnMethods = "allocateAllInCache")
    public void verifyCache2() {
        System.out.println("Verify cache2:");
        cache2.getConfigurationValues().forEach((configurationValue ->
                printCacheConfigurationValue(cache2, configurationValue)
        ));

        long respondingConfigValues = cache2.getConfigurationValues().stream().filter
                (ConfigurationValue::isResponding).count();

        Assert.assertEquals(respondingConfigValues, 0L, "All configuration values in cache 2 must" +
                " be not responding");
    }

    private List<String> generateStrings(int count) {
        List<String> list = new ArrayList<>();
        IntStream.range(0, count).forEach((i) -> list.add(String.valueOf(i)));
        return list;
    }

    private void printCacheConfigurationValue(Cache cache, ConfigurationValue configurationValue) {

        String cacheName = cache.getCacheName();

        boolean responding = configurationValue.isResponding();
        ConfigurationValue.WARN_LEVEL warn_level = configurationValue.getWarnLevel();
        String current = configurationValue.presentCurrentState();
        String special = configurationValue.presentSpecialState();

        StringBuffer messageForUserBuffer = new StringBuffer();
        messageForUserBuffer.append("Cache [").append(cacheName).append("] configuration value:")
                .append(N);
        messageForUserBuffer.append("    ").append((responding ? "configuration is " +
                "FINE" : "configuration IS NOT FINE !")).append(N);
        messageForUserBuffer.append("    ").append("Level: ").append(warn_level).append(N);
        messageForUserBuffer.append("    ").append("current state: ").append(current).append(N);
        messageForUserBuffer.append("    ").append("special state: ").append(special).append(N);

        System.out.println(messageForUserBuffer.toString());
    }

    ObjectsCache objectsCache;

    Cache cache1;
    int cache1Size = 10;
    int cache1DangerLevel = 50;

    Cache cache2;
    int cache2Size = 20;
    int cache2DangerLevel = 90;

    CacheHierarchy cacheHierarchy;
    ObjectCacheStrategy cacheStrategy;
    List<String> listObjectsToCache;
    int objectsToCache = 40;
    Map<String, CachedObject> cachedObjects;
    private static final String N = System.lineSeparator();
}
