package com.stride.service;

import com.stride.entity.User;
import com.stride.exception.BusinessException;
import com.stride.mapper.UserMapper;
import com.stride.util.JwtUtil;
import org.springframework.stereotype.Service;

/**
 * 认证服务：负责登录校验并签发 Token。
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordService passwordService;

    /**
     * 构造器注入。
     *
     * 【为什么用构造器注入而不是 @Autowired 字段注入】
     *   ① 依赖可以被声明为 final，编译期就保证不为 null，对象一旦创建依赖就齐全；
     *   ② 便于单元测试（可以直接 new 出来传 mock 对象，不需要启动 Spring 容器）；
     *   ③ 如果依赖成环，Spring 启动时就会报错，能及早暴露设计问题。
     * Spring 官方文档从 4.3 起就推荐构造器注入，且只有一个构造器时 @Autowired 可以省略。
     */
    public AuthService(UserMapper userMapper, JwtUtil jwtUtil, PasswordService passwordService) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordService = passwordService;
    }

    /**
     * 校验用户名密码，成功则返回 JWT Token。
     *
     * 【安全细节：用户不存在和密码错误返回同一个提示】
     * 如果分别返回"用户不存在"和"密码错误"，攻击者可以借此探测
     * 哪些用户名是真实存在的（这叫"用户名枚举"）。
     * 统一提示可以避免泄露这个信息。
     */
    public AuthResult authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }

        User user = userMapper.selectByUsername(username);

        // 用户不存在，或密码不匹配 → 统一返回 401
        if (user == null || !passwordService.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResult(user, token);
    }

    /**
     * 认证结果：把"用户 + Token"一起返回给上层。
     *
     * 原来只返回 token 字符串，前端登录后还得再发一次请求去查用户信息
     * （authStore.js 里就是这么写的，发了两次请求）。
     * 现在一次返回，少一次往返。
     */
    public record AuthResult(User user, String token) {
    }
}
