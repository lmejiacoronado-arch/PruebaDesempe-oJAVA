package com.hotelnova.exception;
public class DuplicateException extends AppException {
    public DuplicateException(String field, Object value) { super(field + " already exists: " + value); }
}
