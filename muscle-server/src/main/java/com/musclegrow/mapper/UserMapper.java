package com.musclegrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musclegrow.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
