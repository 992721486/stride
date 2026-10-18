package com.stride.common;

/**
 * 统一响应体。
 *
 * 【为什么需要它】
 * 原来接口直接返回实体或裸字符串，导致：
 *   ① 返回 String 时 Spring 用 StringHttpMessageConverter，Content-Type 变成 text/plain，
 *      前端只能把 res.data 当字符串用，将来想加字段（如 refreshToken）就必须改前端；
 *   ② 成功和失败的结构不一致，前端要写两套判断；
 *   ③ 无法携带业务状态码和提示信息。
 *
 * 统一成 { code, msg, data } 之后，前端只需要处理一种结构。
 *
 * @param <T> data 的实际类型（泛型，让 data 可以是对象、集合或 null）
 */
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
