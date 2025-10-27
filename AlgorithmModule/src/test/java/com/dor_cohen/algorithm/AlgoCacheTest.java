package com.dor_cohen.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AlgoCacheTest {

    @Test
    void testLRUEviction() {
        IAlgoCache<Integer, String> cache = new LRUAlgoCacheImpl<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        // access key=1 to make key=2 the LRU
        cache.get(1);
        cache.put(3, "c");  // should evict key 2

        assertNull(cache.get(2), "LRU should evict the least recently used key (2)");
        assertEquals("a", cache.get(1), "Key 1 should still be present");
        assertEquals("c", cache.get(3), "New key 3 should be present");
    }

    @Test
    void testRandomEviction() {
        IAlgoCache<Integer, String> cache = new RandomReplacementAlgoCacheImpl<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");  // randomly evicts either 1 or 2

        // After eviction, exactly two of the three keys must remain:
        int count = 0;
        if (cache.get(1) != null) count++;
        if (cache.get(2) != null) count++;
        if (cache.get(3) != null) count++;
        assertEquals(2, count, "Cache must contain exactly 2 entries after random eviction");

        // And the newly inserted key should always be present:
        assertEquals("c", cache.get(3), "Key 3 must never be evicted immediately");
    }

    @Test
    void testLFUEviction() {
        IAlgoCache<Integer, String> cache = new LFUAlgoCacheImpl<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        // make key=1 more frequently accessed than key=2
        cache.get(1);
        cache.put(3, "c");  // should evict key 2 (least-frequently-used)

        assertNull(cache.get(2), "LFU should evict the least-frequently used key (2)");
        assertEquals("a", cache.get(1), "Key 1 should still be present");
        assertEquals("c", cache.get(3), "New key 3 should be present");
    }
}
