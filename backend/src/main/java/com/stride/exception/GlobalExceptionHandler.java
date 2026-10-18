package com.stride.exception;

import com.stride.common.Result;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * 【@RestControllerAdvice 的作用】
 * 它是 Spring MVC 提供的"切面式"异常拦截：任何 Controller 抛出的异常，
 * 只要匹配到下面某个 @ExceptionHandler 声明的方法，就会被它接管，
 * 统一转换成规范的响应体，而不是让 Spring 返回默认的 500 错误页。
 *
 * 这样 Controller 和 Service 里就不用写 try-catch 了。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常：按异常自带的 code 返回。
     * 例如 401 未认证、403 无权限、404 资源不存在。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        HttpStatus status = HttpStatus.resolve(e.getCode()) != null
                ? HttpStatus.valueOf(e.getCode())
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Result.error(e.getCode(), e.getMessage()));
    }

    /**
     * JWT 相关异常。
     *
     * 【这是修复"token 过期返回 500"的关键】
     * 原来 JwtRequestFilter 里 extractUsername() 在 try-catch 之外，
     * token 过期会抛 ExpiredJwtException（属于 JwtException 的子类），
     * 一路冒泡到 DispatcherServlet 就变成了 500 Internal Server Error。
     * 语义上"token 过期/无效"应该是 401 未认证，所以这里统一映射成 401。
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Result<Void>> handleJwtException(JwtException e) {
        log.warn("JWT 校验失败: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(401, "登录状态已失效，请重新登录"));
    }

    /**
     * 兜底：其他未预料到的异常。
     * 对前端返回统一的 500，但日志里保留完整堆栈，方便排查。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, "服务器内部错误"));
    }
}
