package com.dor_cohen.algorithm;

public abstract class AbstractAlgoCache<K,V> implements IAlgoCache<K,V> {
    protected final int capacity;
    public AbstractAlgoCache(int capacity) {
        this.capacity = capacity;
    }
    protected boolean isFull(int currentSize) {
        return currentSize >= capacity;
    }
}