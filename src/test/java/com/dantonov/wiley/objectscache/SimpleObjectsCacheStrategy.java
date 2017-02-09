package com.dantonov.wiley.objectscache;

import com.dantonov.wiley.objectscache.exceptions.AllocationException;
import com.dantonov.wiley.objectscache.impl.ObjectInCache;

/**
 * Simple implementation for {@link ObjectCacheStrategy}. put object to the first cache in the set
 */
public class SimpleObjectsCacheStrategy implements ObjectCacheStrategy {
    @Override
    public void allocateObject(ObjectInCache objectInCache, CacheHierarchy cacheLevelModel) throws AllocationException {
        cacheLevelModel.getCacheList().stream().findFirst().orElseThrow(AllocationException::new).allocateObject(objectInCache);
    }

    @Override
    public void reallocateObjects(CacheHierarchy cacheLevelModel) {
        // do nothing
    }
}