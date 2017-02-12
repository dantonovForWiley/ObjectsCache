package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.Cache;
import com.dantonov.wiley.objectscache.ConfigurationValue;
import com.dantonov.wiley.objectscache.exceptions.AllocationInCacheException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

/**
 * Test {@link InMemoryCache}
 */
public class InMemoryCacheTest {

    @BeforeTest
    public void initInMemoryCache() throws UnacceptableCacheBuildParameter {
        inMemoryCache = new InMemoryCache.Builder().setMaxCacheSize(MAX_CACHE_SIZE).setDangerLoad
                (DANGER_LOAD).build();
    }

    @Test
    public void exceedMaxCacheValue() {
        IntStream.rangeClosed(1, MAX_CACHE_SIZE + 2).forEach(i -> {
            ObjectInCache objectInCache = new ObjectInCache.Builder().setObject(Integer.valueOf
                    (i)).build();
            try {
                inMemoryCache.allocateObject(objectInCache);
            } catch (AllocationInCacheException e) {
                Assert.assertNull(e, "Allocation exception is not expected");
            }

            long notRespongingValuesCount = inMemoryCache.getConfigurationValues()
                    .stream().filter
                            (ConfigurationValue::isResponding)
                    .count();

            if (i <= DANGER_LOAD) {
                Assert.assertEquals(notRespongingValuesCount, 2, String.format("When " +
                        "inMemoryCache has %s values, there should be two responding " +
                        "configuration values", i));
            } else if (i > DANGER_LOAD && i <= MAX_CACHE_SIZE) {
                Assert.assertEquals(notRespongingValuesCount, 1, String.format("When " +
                        "inMemoryCache has %s values, there should be one responding " +
                        "configuration value", i));
            } else {
                Assert.assertEquals(notRespongingValuesCount, 0, String.format("When " +
                        "inMemoryCache has %s values, there should not be responding " +
                        "configuration values", i));
            }
        });

    }

    private Cache inMemoryCache;
    private final int MAX_CACHE_SIZE = 100;
    private final int DANGER_LOAD = 80;

}
