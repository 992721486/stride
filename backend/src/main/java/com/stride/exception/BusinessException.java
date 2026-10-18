package com.stride.exception;

/**
 * 业务异常。
 *
 * 【为什么需要自定义异常】
 * 原来的代码在出错时直接返回 ResponseEntity.status(401).body("Invalid username/password")，
 * 或者在 Service 里抛 IllegalArgumentException。这有两个问题：
 *   ① 每个 Controller 方法都要写一遍 try-catch，重复代码；
 *   ② 返回给前端的结构不统一，前端要针对每个接口单独判断。
 *
 * 有了自定义异常 + 全局异常处理器（GlobalExceptionHandler）之后：
 *   Service 里只管 throw new BusinessException(401, "用户名或密码错误")，
 *   全局处理器统一转成 { code, msg, data } 格式的响应。
 *
 * 继承 RuntimeException 而不是 Exception：
 * 不强制调用方 try-catch，代码更干净（Spring 的 @Transactional 默认也只对
 * RuntimeException 回滚，保持一致的语义）。
 */
public class BusinessException extends RuntimeException {

    /** 业务状态码，同时会作为 HTTP 状态码返回 */
    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
