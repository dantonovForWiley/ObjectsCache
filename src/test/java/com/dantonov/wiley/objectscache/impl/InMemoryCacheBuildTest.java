package com.dantonov.wiley.objectscache.impl;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test {@link InMemoryCache} building
 */
public class InMemoryCacheBuildTest {

    @DataProvider(name = "unacceptable max size")
    public Object[][] generateUnacceptableMaxSizeValues() {
        return unacceptableMaxSizeValues;
    }

    @DataProvider(name = "acceptable max size")
    public Object[][] generateAcceptableMaxSizeValues() {
        return acceptableMaxSizeValues;
    }

    @Test(expectedExceptions = UnacceptableCacheBuildParameter.class, dataProvider = "unacceptable max size")
    public void configureCacheWithUnacceptableMaxSize(int cacheSize) throws
            UnacceptableCacheBuildParameter {
        new InMemoryCache.Builder().setMaxCacheSize(cacheSize).build();
    }

    @Test(dataProvider = "acceptable max size")
    public void configureCacheWithAcceptableMaxSize(int cacheSize) throws
            UnacceptableCacheBuildParameter {
        new InMemoryCache.Builder().setMaxCacheSize(cacheSize).build();
    }

    @DataProvider(name = "unacceptable danger load")
    public Object[][] generateUnacceptableDangerLoadValues() {
        return unacceptableDangerLoadValues;
    }

    @DataProvider(name = "acceptable danger load")
    public Object[][] generateAcceptableDangerLoadValues() {
        return acceptableDangerLoadValues;
    }

    @Test(expectedExceptions = UnacceptableCacheBuildParameter.class, dataProvider = "unacceptable danger load")
    public void configureCacheWithUnacceptableDangerValue(int dangerLoad) throws
            UnacceptableCacheBuildParameter {
        new InMemoryCache.Builder().setDangerLoad(dangerLoad).build();
    }

    @Test(dataProvider = "acceptable danger load")
    public void configureCacheWithAcceptableDangerValue(int dangerLoad) throws
            UnacceptableCacheBuildParameter {
        new InMemoryCache.Builder().setDangerLoad(dangerLoad).build();
    }

    private Object[][] unacceptableMaxSizeValues = new Object[][]{{-100}, {-20}, {-5}, {-1}, {0}};
    private Object[][] acceptableMaxSizeValues = new Object[][]{{1}, {10}, {100}, {1000}};
    private Object[][] unacceptableDangerLoadValues = new Object[][]{{-100}, {-1}, {101}, {200}};
    private Object[][] acceptableDangerLoadValues = new Object[][]{{0}, {1}, {10}, {60}, {80},
            {99}, {100}};
}
