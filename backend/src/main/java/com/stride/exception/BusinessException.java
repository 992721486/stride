package com.stride.exception;
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    public BusinessException(String message) {
        this(400, message);            // this(...) 复用上面的双参构造器
    }

    public Integer getCode() {
        return code;
    }
}
