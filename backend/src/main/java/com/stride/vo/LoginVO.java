package com.stride.vo;

/**
 * 登录成功返回给前端的对象。
 *
 * 不再像原来那样直接返回一个裸的 token 字符串（Content-Type 会是 text/plain），
 * 而是放在统一响应体里，将来要加过期时间、refreshToken、用户信息都很容易。
 */
public class LoginVO {

    private String token;
    private UserVO user;

    public LoginVO() {
    }

    public LoginVO(String token, UserVO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserVO getUser() { return user; }
    public void setUser(UserVO user) { this.user = user; }
}
