package com.dor_cohen.app.dao;

import com.google.gson.Gson;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;


public class DaoFileImpl<T> implements IDao<T> {
    private final Path path; private final Gson gson = new Gson(); private final Type type;
    public DaoFileImpl(String filePath, Type type) {
        this.path = Paths.get(filePath);
        this.type = type;
    }
    private Map<Long, T> readAll() {
        try { if (!Files.exists(path)) return new HashMap<>();
            try (Reader r=Files.newBufferedReader(path)) {
                Map<Long,T> map = gson.fromJson(r, type);
                return map!=null?map:new HashMap<>();
            }
        } catch (Exception e) { e.printStackTrace(); return new HashMap<>(); }
    }
    private void writeAll(Map<Long, T> map) {
        try (Writer w=Files.newBufferedWriter(path)) {
            gson.toJson(map, w);
        } catch(Exception e){ e.printStackTrace(); }
    }
    @Override public void save(T obj) {
        try {
            var map = readAll();
            var id = (Long)obj.getClass().getMethod("getId").invoke(obj);
            map.put(id, obj);
            writeAll(map);
        } catch(Exception e){ e.printStackTrace(); }
    }
    @Override public T load(Long id) {
        return readAll().get(id);
    }
    @Override public void delete(Long id) {
        var map = readAll();
        map.remove(id); writeAll(map);
    }
    public List<T> loadAll() {
        return new ArrayList<>( readAll().values() );
    }
}
