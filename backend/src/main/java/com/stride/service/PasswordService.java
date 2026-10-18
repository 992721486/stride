package com.stride.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具。
 *
 * 【为什么不能明文存密码】
 * 数据库一旦泄露（备份泄露、被拖库、开发同学导数据），所有用户的密码就直接暴露。
 * 而且用户往往在多个网站用同一个密码，泄露会影响他在别的网站的安全。
 *
 * 【为什么用 BCrypt 而不是 MD5/SHA】
 *   ① MD5/SHA 是"快"哈希，用显卡每秒能算几十亿次，弱密码几秒就被撞出来；
 *      BCrypt 是"慢"哈希，故意设计得慢（可通过 cost 参数调难度），暴力破解成本极高。
 *   ② MD5 同样的输入永远得到同样的输出。如果两个人密码相同，密文也相同，
 *      攻击者可以直接看出"这两个人密码一样"。
 *      BCrypt 每次加密都会随机生成一个"盐值"混进去，所以同一个密码每次加密结果都不同。
 *   ③ BCrypt 把盐值和 cost 因子直接编码在结果字符串里，不需要额外存字段。
 *      形如：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
 *           └┬┘└┬┘└──────────────────────┬──────────────────────┘
 *         算法 cost        盐值 + 密文
 *
 * 【matches 的用法】不能把密文解密回原文（哈希不可逆），
 * 只能用 matches(明文, 密文) 让 BCrypt 用同样的盐值再算一次来比对。
 */
@Component
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密明文密码（注册时用）。
     */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 校验明文密码是否与密文匹配（登录时用）。
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }
}
