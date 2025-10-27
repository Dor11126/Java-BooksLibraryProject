package com.dor_cohen.algorithm;

/**
 * Generic cache interface defining basic operations.
 */
public interface IAlgoCache<K,V> {
    /**
     * Retrieve the value associated with the given key, or null if absent.
     */
    V get(K key);

    /**
     * Insert or update the value for the given key.
     */
    void put(K key, V value);

    /**
     * Evict one key according to this algorithm’s policy, returning the evicted key.
     * The service layer may use this to remove entries when capacity is reached.
     */
    K evictKey();
}