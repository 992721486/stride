package com.stride.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户实体，对应数据库表 users。
 */
@TableName("users")
public class User {

    /**
     * 主键。
     *
     * 【为什么必须加 @TableId(type = IdType.AUTO)】
     * MyBatis-Plus 在主键字段没有 @TableId 注解时，默认按 IdType.ASSIGN_ID 处理，
     * 即用雪花算法（Snowflake）生成 64 位 long 作为主键。
     * 但数据库里 users.id 是 INT（32 位）且为 AUTO_INCREMENT，
     * long 塞进 INT 会被截断，结果就是负数 id（例如 -1250058239）。
     *
     * IdType 四种取值：
     *   AUTO        数据库自增（表里是 auto_increment 时用它）← 本项目
     *   ASSIGN_ID   雪花算法生成 long（默认值，适合主键为 bigint 的场景）
     *   ASSIGN_UUID 生成 32 位 UUID 字符串（主键为 varchar 时）
     *   NONE        跟随全局配置
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String username;
    private String password;
    private String email;
    private Integer role;

    // Getter 和 Setter 方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
}