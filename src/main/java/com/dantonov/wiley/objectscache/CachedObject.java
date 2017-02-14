package com.dantonov.wiley.objectscache;

import com.dantonov.wiley.objectscache.exceptions.AllocationInCacheException;
import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;

import java.util.UUID;

/**
 * Top level abstraction for reference to cached object.
 */
public interface CachedObject {
    /**
     * Method to retrieve object instance associated with this {@link CachedObject} from corresponding {@link ObjectsCache}
     *
     * @return {@link Object} object from cache
     * @throws ObjectNotFoundInCache      in case when object has not been found
     * @throws AllocationInCacheException in case when underlying cache has failed to retrieve
     *                                    the cached object
     */
    Object getObject() throws ObjectNotFoundInCache, AllocationInCacheException;

    /**
     * Identifier for cached object
     *
     * @return {@link UUID} uuid
     */
    UUID getUuid();
}
