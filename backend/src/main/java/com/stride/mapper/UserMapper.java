package com.stride.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stride.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户表数据访问层。
 *
 * 继承 MyBatis-Plus 的 BaseMapper 后，自动拥有 insert / selectById /
 * selectList / updateById / deleteById 等基础方法，无需自己写 SQL。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户。
     *
     * 【#{} 与 ${} 的区别（高频面试题）】
     *   #{}  编译成 JDBC PreparedStatement 的占位符 ?，参数作为"值"传入，
     *        数据库不会把它当 SQL 解析 → 天然防 SQL 注入；
     *   ${}  字符串直接拼接进 SQL，有注入风险，只在传表名/列名时不得已才用。
     * 这里用 #{} 是正确写法。
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
}
