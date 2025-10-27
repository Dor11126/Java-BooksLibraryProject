package com.dor_cohen.algorithm;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUAlgoCacheImpl<K,V> extends AbstractAlgoCache<K,V> {
    private final LinkedHashMap<K,V> cache;
    public LRUAlgoCacheImpl(int capacity) {
        super(capacity);
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
                return size() > LRUAlgoCacheImpl.this.capacity;
            }
        };
    }
    @Override public V get(K key) { return cache.get(key); }
    @Override public void put(K key, V value) {
        cache.put(key, value);
    }
    @Override public K evictKey() {
        K eldest = cache.keySet().iterator().next();
        cache.remove(eldest);
        return eldest;
    }
}