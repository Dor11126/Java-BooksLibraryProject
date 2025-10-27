package com.dor_cohen.algorithm;

import java.util.*;

public class LFUAlgoCacheImpl<K,V> extends AbstractAlgoCache<K,V> {
    private final Map<K, V> cache = new HashMap<>();
    private final Map<K, Integer> freq = new HashMap<>();
    public LFUAlgoCacheImpl(int capacity) {
        super(capacity);
    }
    @Override public V get(K key) {
        if (cache.containsKey(key)) {
            freq.put(key, freq.get(key) + 1);
        }
        return cache.get(key);
    }
    @Override public void put(K key, V value) {
        if (isFull(cache.size()) && !cache.containsKey(key)) {
            K evict = evictKey();
            cache.remove(evict);
            freq.remove(evict);
        }
        cache.put(key, value);
        freq.put(key, freq.getOrDefault(key, 0) + 1);
    }
    @Override public K evictKey() {
        return Collections.min(freq.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}