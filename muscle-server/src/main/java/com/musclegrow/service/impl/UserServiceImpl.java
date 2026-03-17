package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musclegrow.constant.MessageConstant;
import com.musclegrow.constant.StatusConstant;
import com.musclegrow.dto.UserLoginDTO;
import com.musclegrow.dto.UserRegisterDTO;
import com.musclegrow.entity.User;
import com.musclegrow.exception.AccountLockedException;
import com.musclegrow.exception.AccountNotFoundException;
import com.musclegrow.exception.BaseException;
import com.musclegrow.exception.PasswordErrorException;
import com.musclegrow.mapper.UserMapper;
import com.musclegrow.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, userRegisterDTO.getPhone());
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            throw new BaseException("手机号已注册");
        }

        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            throw new BaseException("两次输入的密码不一致");
        }

        User registerUser = new User();
        BeanUtils.copyProperties(userRegisterDTO, registerUser);
        registerUser.setPassword(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()));
        registerUser.setStatus(StatusConstant.ENABLE);
        registerUser.setCreateTime(LocalDateTime.now());
        registerUser.setUpdateTime(LocalDateTime.now());
        userMapper.insert(registerUser);
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, userLoginDTO.getPhone());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        String password = DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes());
        if (!password.equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus() != null && user.getStatus().equals(StatusConstant.DISABLE)) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        return user;
    }
}
