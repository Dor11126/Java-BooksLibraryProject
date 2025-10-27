package com.dor_cohen.app.dm;

public class Book {
    private Long id; private String title; private String author;
    public Book() {}
    public Book(Long id, String title, String author) {
        this.id = id; this.title = title; this.author = author;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getAuthor() { return author; }
    public void setAuthor(String a) { this.author = a; }
}