package com.dor_cohen.app.service;

import com.dor_cohen.app.dao.IDao;
import com.dor_cohen.app.dm.Book;
import com.dor_cohen.algorithm.IAlgoCache;
import java.util.List;
import com.dor_cohen.app.dao.DaoFileImpl;


public class BookService {
    private final IDao<Book> dao;
    private final IAlgoCache<Long, Book> cache;
    public BookService(IDao<Book> dao, IAlgoCache<Long, Book> cache) {
        this.dao = dao; this.cache = cache;
    }
    public Book findBook(Long id) {
        Book b = cache.get(id);
        if (b == null) { b = dao.load(id); if (b!=null) cache.put(id,b); }
        return b;
    }
    public void saveBook(Book b) {
        dao.save(b); cache.put(b.getId(), b);
    }
    public void deleteBook(Long id) {
        dao.delete(id);
        // no direct removal in IAlgoCache
    }
    public List<Book> getAllBooks() {
        // Assuming DaoFileImpl now has loadAll()
        return ((DaoFileImpl<Book>) dao).loadAll();
    }
}