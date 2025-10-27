package com.dor_cohen.app.dao;

public interface IDao<T> {
    void save(T obj);
    T load(Long id);
    void delete(Long id);
}