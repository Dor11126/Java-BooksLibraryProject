package com.dor_cohen.network.controller;
import com.dor_cohen.app.service.BookService;
import com.dor_cohen.app.dm.Book;
import com.dor_cohen.network.dto.Response;
import java.util.List;
public class BookController {
    private final BookService service;
    public BookController(BookService svc) { this.service = svc; }
    public Response<Book> add(Book b) {
        service.saveBook(b);
        Response<Book> r = new Response<>(); r.success=true; r.data=b; return r;
    }
    public Response<Book> get(Long id) {
        Book b = service.findBook(id);
        Response<Book> r = new Response<>();
        if (b!=null) { r.success=true; r.data=b; } else { r.success=false; r.message="Not found"; }
        return r;
    }
    public Response<String> delete(Long id) {
        service.deleteBook(id);
        Response<String> r = new Response<>(); r.success=true; r.message="Deleted"; return r;
    }
    public Response<List<Book>> getAll() {
        List<Book> all = service.getAllBooks();
        Response<List<Book>> r = new Response<>();
        r.success = true;
        r.data = all;
        return r;
    }
}