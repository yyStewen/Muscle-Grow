package com.musclegrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musclegrow.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {


}
