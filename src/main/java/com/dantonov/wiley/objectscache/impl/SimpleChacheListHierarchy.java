package com.dantonov.wiley.objectscache.impl;

import com.dantonov.wiley.objectscache.Cache;
import com.dantonov.wiley.objectscache.CacheHierarchy;

import java.util.ArrayList;
import java.util.List;

/**
 * Models levels of {@link Cache} caches
 */
public class SimpleChacheListHierarchy implements CacheHierarchy {

    private SimpleChacheListHierarchy(List<Cache> caches) {
        this.caches = caches;
    }

    /**
     * Builder for {@link SimpleChacheListHierarchy}
     */
    public static class Builder {

        /**
         * Add a new {@link Cache} instance. New element will be located in the end of the list of caches
         *
         * @param cache new {@link Cache} instance to ba added to the cache list
         * @return {@link Builder} to support build chaining
         */
        public Builder addCache(Cache cache) {
            caches.add(cache);
            return this;
        }

        /**
         * Build {@link SimpleChacheListHierarchy}
         *
         * @return a new {@link SimpleChacheListHierarchy} instance
         */
        public SimpleChacheListHierarchy build() {
            return new SimpleChacheListHierarchy(caches);
        }

        private List<Cache> caches = new ArrayList<>();
    }

    @Override
    public List<Cache> getCacheList() {
        return new ArrayList<>(caches);
    }

    private List<Cache> caches;

}
