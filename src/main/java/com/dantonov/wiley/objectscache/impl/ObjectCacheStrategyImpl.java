package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.*;
import com.dantonov.wiley.objectscache.exceptions.AllocationException;
import com.dantonov.wiley.objectscache.exceptions.AllocationInCacheException;
import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;

import java.util.*;

/**
 * Basic implementation for {@link ObjectCacheStrategy}
 * <ul>Details of implementation:
 * <li>
 * {@link ObjectCacheStrategyImpl#allocateObject(ObjectInCache, CacheHierarchy)}<br>
 * Put object to the first cache in {@link CacheHierarchy}<br>
 * - if any critical configuration value is not responding - try to put the object to the
 * next cache<br>
 * - if object has not been allocated in the cache - try to put the object to the
 * next cache<br>
 * </li>
 * <li>
 * {@link ObjectCacheStrategyImpl#reallocateObjects(CacheHierarchy)}<br>
 * Move objects from cache to next cache in {@link CacheHierarchy} till any
 * {@link com.dantonov.wiley.objectscache.ConfigurationValue.WARN_LEVEL#WARN} or
 * {@link com.dantonov.wiley.objectscache.ConfigurationValue.WARN_LEVEL#CRITICAL} is not
 * responding<br>
 * - do this for each cache in {@link CacheHierarchy}
 * </li>
 * <li>
 * {@link ObjectCacheStrategyImpl#releaseObject(ObjectInCache, CacheHierarchy)}<br>
 * Remove object from cache
 * </li>
 * </ul>
 */
public class ObjectCacheStrategyImpl implements ObjectCacheStrategy {

    /**
     * Constructor for {@link ObjectCacheStrategyImpl}
     */
    public ObjectCacheStrategyImpl() {
        cachedObjects = new LinkedList<>();
    }

    @Override
    public void allocateObject(ObjectInCache objectInCache, CacheHierarchy cacheHierarchy)
            throws AllocationException {
        synchronized (cachedObjects) {
            Iterator<Cache> cacheIterator = cacheHierarchy.getCacheList().iterator();
            while (cacheIterator.hasNext()) {
                Cache cache = cacheIterator.next();
                if (getNotRespondingConfigurationValues(cache, Arrays.asList(ConfigurationValue
                        .WARN_LEVEL.CRITICAL)) == 0) {
                    // everything is fine. can put here
                    allocate(cache, objectInCache);
                    return;
                } else {
                    if (!cacheIterator.hasNext()) {
                        // there is at least one not responding critical configuration value, but
                        // this is a last cache in hierarchy. so try to put here anyway
                        allocate(cache, objectInCache);
                        return;
                    }
                }
            }
            throw new AllocationException();
        }
    }

    @Override
    public void releaseObject(ObjectInCache objectInCache, CacheHierarchy
            cacheHierarchy) {
        synchronized (cachedObjects) {
            cacheHierarchy.getCacheList().stream().filter((cache) -> cache.contains(objectInCache))
                    .forEach((cache -> {
                        try {
                            cache.freeObject(objectInCache);
                        } catch (ObjectNotFoundInCache | AllocationInCacheException e) {
                            // TODO: here we should log exception
                        }
                    }));
        }
    }

    @Override
    public void reallocateObjects(CacheHierarchy cacheHierarchy) {
        synchronized (cachedObjects) {
            Iterator<Cache> cacheIterator = cacheHierarchy.getCacheList().iterator();
            while (cacheIterator.hasNext()) {
                Cache cache = cacheIterator.next();
                boolean isLastCache = !cacheIterator.hasNext();
                boolean itHasNotRespondingConfigValues = getNotRespondingConfigurationValues(cache,
                        Arrays.asList(ConfigurationValue.WARN_LEVEL
                                .CRITICAL, ConfigurationValue.WARN_LEVEL.WARN)) > 0;

                if (itHasNotRespondingConfigValues && !isLastCache) {
                    // move objects to next cache until configuration values will not exist

                    Cache nextCache = getNextCache(cache, cacheHierarchy);

                    for (UUID cachedObjectUuid : cachedObjects) {
                        if (getNotRespondingConfigurationValues(cache,
                                Arrays.asList(ConfigurationValue.WARN_LEVEL
                                        .CRITICAL, ConfigurationValue.WARN_LEVEL.WARN)) > 0) {
                            ObjectInCache retrievedObjectInCache = null;
                            try {
                                retrievedObjectInCache = cache.freeObject(ObjectInCache
                                        .from(cachedObjectUuid));
                            } catch (ObjectNotFoundInCache | AllocationInCacheException e) {
                                // TODO: here we should log exception
                            }
                            if (retrievedObjectInCache != null) {
                                try {
                                    nextCache.allocateObject(retrievedObjectInCache);
                                } catch (AllocationInCacheException e) {
                                    // TODO: here we should log exception
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    private Cache getNextCache(Cache currentCache, CacheHierarchy cacheHierarchy) {
        Iterator<Cache> iterator = cacheHierarchy.getCacheList().iterator();
        while (iterator.hasNext()) {
            Cache cache = iterator.next();
            if (cache == currentCache && iterator.hasNext()) {
                return iterator.next();
            }
        }
        return null;
    }

    private void allocate(Cache cache, ObjectInCache objectInCache) throws AllocationInCacheException {
        cache.allocateObject(objectInCache);
        cachedObjects.add(objectInCache.getUuid());
    }

    private long getNotRespondingConfigurationValues(Cache cache, List<ConfigurationValue
            .WARN_LEVEL>
            warn_levels) {
        return warn_levels.stream().mapToLong((warn_level) -> cache.getConfigurationValues().stream
                ().filter((configurationValue -> configurationValue.getWarnLevel().equals
                (warn_level))).filter((configurationValue -> !configurationValue
                .isResponding())).count()).sum();
    }

    private List<UUID> cachedObjects;
}
