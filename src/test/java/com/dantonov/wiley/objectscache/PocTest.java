package com.dantonov.wiley.objectscache;

import com.dantonov.wiley.objectscache.exceptions.AllocationException;
import com.dantonov.wiley.objectscache.exceptions.AllocationInCacheException;
import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;
import com.dantonov.wiley.objectscache.impl.ObjectCacheImpl;
import com.dantonov.wiley.objectscache.impl.SimpleChacheListHierarchy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Proof of concept
 */
public class PocTest {
    @Test
    public void poc(){

        SimpleMemoryCache simpleMemoryCache = new SimpleMemoryCache.Builder().setMaxObjectSize(10).build();

        CacheHierarchy levelModel = new SimpleChacheListHierarchy.Builder().addCache(simpleMemoryCache).build();

        ObjectsCache cache1 = new ObjectCacheImpl.Builder().setCacheLevelModel(levelModel).setCacheMovingStrategy(new SimpleObjectsCacheStrategy()).build();

        List<String> stringList = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "I", "J", "K", "L", "M", "O");

        List<CachedObject> cachedStrings = new ArrayList<>();

        stringList.forEach((str) -> {
            try {
                logCacheStatus(levelModel);
                LOGGER.info(String.format("adding object [%s] to cache",str));
                cachedStrings.add(cache1.cacheObject(str));
            } catch (AllocationException e) {
                e.printStackTrace();
            }
        });

        logCacheStatus(levelModel);

        LOGGER.info("Print cached objects");

        cachedStrings.forEach((x)-> {

            try {
                LOGGER.info(String.format("cached object: id = %s, str = %s",x.getUuid(),String.valueOf(x.getObject())));
            } catch (ObjectNotFoundInCache | AllocationInCacheException objectNotFoundInCache) {
                objectNotFoundInCache.printStackTrace();
            }

        });

    }

    private static void logCacheStatus(CacheHierarchy levelModel){
        StringBuffer statusBuffer = new StringBuffer();
        statusBuffer.append("Printing cache status").append(N);
        levelModel.getCacheList().forEach((cache -> {
            statusBuffer.append(String.format("@cache [%s] status:",cache.getCacheName())).append(N);;
            cache.getConfigurationValues().forEach((configurationValue -> {
                statusBuffer.append(String.format("   configuration [%s] value %s:",configurationValue.getWarnLevel(),(configurationValue.isResponding()?"is FINE":"is NOT FINE    !"))).append(N);
                statusBuffer.append(String.format("       configured value: %s",configurationValue.presentSpecialState())).append(N);
                statusBuffer.append(String.format("       current value: %s",configurationValue.presentCurrentState())).append(N);
            }));
        }));
        statusBuffer.append(N);
        LOGGER.info(statusBuffer.toString());
    }

    private static final String N = System.lineSeparator();
    private static final Logger LOGGER = LoggerFactory.getLogger(PocTest.class);
}
