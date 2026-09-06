package com.antarang.cap.exception;

public class BusinessException extends RuntimeException {

    private final String code;
    private final Object data;

    public BusinessException(String message, String code) {
        this(message, code, null);
    }

    public BusinessException(String message, String code, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }
}
