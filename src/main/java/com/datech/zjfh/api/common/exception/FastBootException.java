package com.datech.zjfh.api.common.exception;

public class FastBootException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FastBootException(String message){
        super(message);
    }

    public FastBootException(Throwable cause)
    {
        super(cause);
    }

    public FastBootException(String message, Throwable cause)
    {
        super(message,cause);
    }

}
