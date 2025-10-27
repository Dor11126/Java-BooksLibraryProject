package com.dor_cohen.app.service;

import com.dor_cohen.app.dm.Book;
import com.dor_cohen.app.dao.DaoFileImpl;
import com.dor_cohen.algorithm.LRUAlgoCacheImpl;
import org.junit.jupiter.api.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {
    private BookService service;
    private Path dbFile;
    @BeforeEach void setup() throws Exception {
        dbFile = Path.of("test_books.json");
        Files.deleteIfExists(dbFile);
        var dao = new DaoFileImpl<Book>(dbFile.toString(), (Type)Book.class.getGenericSuperclass());
        var cache = new LRUAlgoCacheImpl<Long, Book>(2);
        service = new BookService(dao, cache);
    }
    @Test void testSaveAndFind() {
        Book b = new Book(1L,"T","A");
        service.saveBook(b);
        assertEquals("T", service.findBook(1L).getTitle());
    }
    @AfterEach void cleanup() throws Exception {
        Files.deleteIfExists(dbFile);
    }
}