package com.dor_cohen.algorithm;

import java.util.*;

public class RandomReplacementAlgoCacheImpl<K,V> extends AbstractAlgoCache<K,V> {
    private final Map<K,V> cache = new HashMap<>();
    private final Random random = new Random();
    public RandomReplacementAlgoCacheImpl(int capacity) {
        super(capacity);
    }
    @Override public V get(K key) {
        return cache.get(key);
    }
    @Override public void put(K key, V value) {
        if (isFull(cache.size()) && !cache.containsKey(key)) {
            List<K> keys = new ArrayList<>(cache.keySet());
            K evict = keys.get(random.nextInt(keys.size()));
            cache.remove(evict);
        }
        cache.put(key, value);
    }
    @Override public K evictKey() {
        List<K> keys = new ArrayList<>(cache.keySet());
        K evict = keys.get(random.nextInt(keys.size()));
        cache.remove(evict);
        return evict;
    }
}