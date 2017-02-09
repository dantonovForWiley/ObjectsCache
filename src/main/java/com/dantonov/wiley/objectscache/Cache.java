package com.dantonov.wiley.objectscache;

import com.dantonov.wiley.objectscache.exceptions.ObjectNotFoundInCache;
import com.dantonov.wiley.objectscache.impl.ObjectInCache;

import java.util.Set;

/**
 * Models particular cache implementation.
 * It can store objects and provide its {@link ConfigurationValue} configuration values
 */
public interface Cache {

    /**
     * Method to retrieve {@link ConfigurationValue} properties values for this {@link Cache}
     *
     * @return {@link Set} of {@link ConfigurationValue} properties values
     */
    Set<ConfigurationValue> getConfigurationValues();

    /**
     * Method to allocate the object, referenced by {@link ObjectInCache}
     *
     * @param cachedObject
     */
    void allocateObject(ObjectInCache cachedObject);

    /**
     * Method to remove the object, referenced by {@link ObjectInCache}, from this {@link Cache}
     *
     * @param cachedObject {@link ObjectInCache} reference to the cached object to be removed from this cache
     * @return {@link ObjectInCache} reference to the cached object
     * @throws ObjectNotFoundInCache in case then object has not been found
     */
    ObjectInCache freeObject(ObjectInCache cachedObject) throws ObjectNotFoundInCache;

    /**
     * Method to return object from the {@link Cache}. Object is still stored in the cache.
     *
     * @param cachedObject {@link ObjectInCache} reference to the cached object to be retrieved from this cache
     * @return {@link ObjectInCache} reference to the cached object
     * @throws ObjectNotFoundInCache in case then object has not been found
     */
    ObjectInCache returnObject(ObjectInCache cachedObject) throws ObjectNotFoundInCache;

    /**
     * Method to verify if the object, referenced by {@link ObjectInCache}, is existing in this {@link Cache}
     *
     * @param objectInCache {@link ObjectInCache} reference to the cached object to be checked
     * @return {@link Boolean}<br>
     * <code>true</code> - object exists<br>
     * <code>false</code> - object does not exist - it does not cached by this {@link Cache}
     */
    Boolean contains(ObjectInCache objectInCache);

    /**
     * Method to get {@link Cache} name
     *
     * @return {@link String} the name of the {@link Cache}
     */
    default String getCacheName() {
        return "Nonamed cache # " + this.hashCode();
    }
}
