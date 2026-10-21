package com.stride.service;

import com.stride.entity.User;
import com.stride.exception.BusinessException;//自定义的业务报错类
import com.stride.mapper.UserMapper;//查数据库的接口
import com.stride.util.JwtUtil;//生成Token的工具类
import org.springframework.stereotype.Service;//Spring的注解，表示这是一个服务类

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordService passwordService;

    public AuthService(UserMapper userMapper, JwtUtil jwtUtil, PasswordService passwordService) {
        this.userMapper = userMapper;
        this.passwordService = passwordService;
        this.jwtUtil = jwtUtil;
    }

    public AuthResult authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }
        User user = userMapper.selectByUsername(username);
        if (user == null || !passwordService.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user);
        return new AuthResult(user, token);
    }

    public record AuthResult(User user, String token) {
    }
}
