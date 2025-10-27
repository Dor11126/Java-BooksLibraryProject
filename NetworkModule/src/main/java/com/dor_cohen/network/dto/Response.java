package com.dor_cohen.network.dto;
public class Response<T> {
    public boolean success;
    public String message;
    public T data;
}