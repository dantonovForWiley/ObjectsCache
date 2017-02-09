package com.dantonov.wiley.objectscache.exceptions;

import com.dantonov.wiley.objectscache.CachedObject;

/**
 * Common exception for case when object has not been found in cache
 */
public class ObjectNotFoundInCache extends Exception {
    /**
     * Simple constructor for {@link ObjectNotFoundInCache}
     *
     * @param objectInCache {@link CachedObject} instance referencing to object
     */
    public ObjectNotFoundInCache(CachedObject objectInCache) {
        super(objectInCache.getUuid().toString());
    }
}
