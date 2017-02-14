package com.dantonov.wiley.objectscache;

import com.dantonov.wiley.objectscache.exceptions.AllocationException;
import com.dantonov.wiley.objectscache.impl.ObjectInCache;

/**
 * Models behaviour of storing objects in {@link CacheHierarchy}.<br>
 * {@link ObjectCacheStrategy} is responsible for moving objects between underlying {@link Cache} caches
 */
public interface ObjectCacheStrategy {
    /**
     * Method to implement allocation of a new object in {@link CacheHierarchy}
     *
     * @param objectInCache  {@link ObjectInCache} reference for cached object
     * @param cacheHierarchy {@link CacheHierarchy} representing underlying caches
     * @throws AllocationException in case if object has not been allocated
     */
    void allocateObject(ObjectInCache objectInCache, CacheHierarchy cacheHierarchy) throws AllocationException;

    /**
     * Method to implement releasing of cached object
     *
     * @param objectInCache  {@link ObjectInCache} reference for cached object
     * @param cacheHierarchy {@link CacheHierarchy} representing underlying caches
     */
    default void releaseObject(ObjectInCache objectInCache, CacheHierarchy cacheHierarchy) {
        // do nothing. illustrate interface expansion without modifying extended instances
    }

    /**
     * Method to implement objects' migration between underlying caches from {@link CacheHierarchy}
     *
     * @param cacheHierarchy {@link CacheHierarchy} representing underlying caches
     */
    void reallocateObjects(CacheHierarchy cacheHierarchy);
}
