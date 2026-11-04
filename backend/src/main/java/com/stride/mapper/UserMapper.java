package com.stride.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stride.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User>{
    @Select("Select*From users where username=#{username}")
    User selectByUsername(@Param("username")String username);
}

