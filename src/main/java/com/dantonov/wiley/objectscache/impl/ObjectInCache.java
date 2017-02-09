package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.CachedObject;
import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;

import java.util.UUID;

/**
 * Represents particular cached object reference.<br>
 * Internal entity for implementation.<br>
 * May contains an actual object.<br>
 * Implements {@link CachedObject#getObject()} method to get the object from {@link com.dantonov.wiley.objectscache.ObjectsCache} implementation
 */
public class ObjectInCache implements CachedObject {

    private ObjectInCache(Object object, ObjectCacheImpl objectsCache, UUID uuid) {
        this.object = object;
        this.objectsCache = objectsCache;
        this.uuid = uuid;
    }

    /**
     * Builder for {@link ObjectInCache}
     */
    public static class Builder {

        /**
         * Set {@link Object}
         *
         * @param object object managed by the {@link ObjectInCache}
         * @return {@link Builder} to support build chaining
         */
        Builder setObject(Object object) {
            this.object = object;
            return this;
        }

        /**
         * Set appropriate {@link ObjectCacheImpl} that manages caches
         *
         * @param objectsCache {@link ObjectCacheImpl} instance that will be used to retrieve {@link Object} for the particular {@link ObjectInCache} instance
         * @return {@link Builder} to support build chaining
         */
        Builder setObjectsCache(ObjectCacheImpl objectsCache) {
            this.objectsCache = objectsCache;
            return this;
        }

        /**
         * Build {@link ObjectInCache} instance
         *
         * @return new {@link ObjectInCache} instance
         */
        public ObjectInCache build() {
            return new ObjectInCache(this.object, this.objectsCache, UUID.randomUUID());
        }

        private Object object;
        private ObjectCacheImpl objectsCache;
    }

    /**
     * Support copying for {@link ObjectInCache} instances
     *
     * @param objectInCache {@link ObjectInCache} instance to copy
     * @param actualObject  {@link Object} to be managed by new {@link ObjectInCache} instances
     * @return new {@link ObjectInCache} instances
     */
    public static ObjectInCache from(ObjectInCache objectInCache, Object actualObject) {
        return new ObjectInCache(actualObject, objectInCache.objectsCache, objectInCache.uuid);
    }

    @Override
    public Object getObject() throws ObjectNotFoundInCache {
        return objectsCache.findObject(this);
    }

    /**
     * Method to retrieve {@link Object} directly from {@link ObjectInCache} instances
     * @return
     */
    public Object objectRef() {
        return object;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    private ObjectCacheImpl objectsCache;
    private Object object;
    private UUID uuid;
}