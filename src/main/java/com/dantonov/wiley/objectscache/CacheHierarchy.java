package com.dantonov.wiley.objectscache;

import java.util.List;

/**
 * Models hierarchy of {@link Cache} caches
 */
public interface CacheHierarchy {
    /**
     * Provide a {@link List} of {@link Cache} instances.
     * First element in the list is a top level cache.
     *
     * @return {@link List} of {@link Cache} instances
     */
    List<Cache> getCacheList();
}
