package com.stride.service;

import com.stride.entity.User;
import com.stride.exception.BusinessException;
import com.stride.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户业务服务。
 */
@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordService passwordService;

    public UserService(UserMapper userMapper, PasswordService passwordService) {
        this.userMapper = userMapper;
        this.passwordService = passwordService;
    }

    /**
     * 注册用户。
     *
     * 【安全要点：role 由后端决定，不能让前端传】
     * 原来是 Register.vue 里硬编码 role: 2，但这并不可靠——
     * 攻击者用 Postman 直接发 {"username":"x","password":"y","role":1}
     * 就能把自己注册成管理员。前端传的任何值都不可信，权限必须由后端确定。
     * 所以这里强制把 role 设为 2（普通用户）。
     */
    @Transactional
    public User register(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new BusinessException(400, "密码长度不能少于 6 位");
        }
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new BusinessException(409, "用户名已存在");
        }

        // 密码加密后入库，数据库里不再有明文密码
        user.setPassword(passwordService.encode(user.getPassword()));
        // 强制普通用户，忽略前端传来的任何 role 值
        user.setRole(2);

        userMapper.insert(user);
        return user;
    }

    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public User findById(Integer id) {
        return userMapper.selectById(id);
    }

    public List<User> findAllUsers() {
        return userMapper.selectList(null);
    }

    /**
     * 修改用户信息。
     *
     * 【注意没有修改 password 和 role】
     * 用户信息修改和密码修改应该是两个独立接口，混在一起容易出现
     * "改邮箱时顺带把密码覆盖成空"这类问题。这里只允许改邮箱。
     */
    @Transactional
    public User updateUser(Integer id, User updates) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        existing.setEmail(updates.getEmail());
        userMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public void deleteUser(Integer id) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        userMapper.deleteById(id);
    }
}
