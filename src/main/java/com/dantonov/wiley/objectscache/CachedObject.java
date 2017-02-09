package com.dantonov.wiley.objectscache;

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
     * @throws ObjectNotFoundInCache in case if object has not been found
     */
    Object getObject() throws ObjectNotFoundInCache;

    /**
     * Identifier for cached object
     *
     * @return {@link UUID} uuid
     */
    UUID getUuid();
}
