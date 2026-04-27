package com.hotelnova.exception;
public class NotFoundException extends AppException {
    public NotFoundException(String entity, Object id) { super(entity + " not found: " + id); }
    public NotFoundException(String msg) { super(msg); }
}
