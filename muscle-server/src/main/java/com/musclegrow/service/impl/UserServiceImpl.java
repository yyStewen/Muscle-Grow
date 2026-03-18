package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musclegrow.constant.MessageConstant;
import com.musclegrow.constant.StatusConstant;
import com.musclegrow.context.BaseContext;
import com.musclegrow.dto.UserLoginDTO;
import com.musclegrow.dto.UserPasswordUpdateDTO;
import com.musclegrow.dto.UserRegisterDTO;
import com.musclegrow.entity.User;
import com.musclegrow.exception.AccountLockedException;
import com.musclegrow.exception.AccountNotFoundException;
import com.musclegrow.exception.BaseException;
import com.musclegrow.exception.PasswordEditFailedException;
import com.musclegrow.exception.PasswordErrorException;
import com.musclegrow.mapper.UserMapper;
import com.musclegrow.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

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

    @Override
    public void updatePassword(UserPasswordUpdateDTO userPasswordUpdateDTO) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }

        if (!StringUtils.hasText(userPasswordUpdateDTO.getOldPassword())
                || !StringUtils.hasText(userPasswordUpdateDTO.getNewPassword())
                || !StringUtils.hasText(userPasswordUpdateDTO.getConfirmPassword())) {
            throw new PasswordEditFailedException("密码不能为空");
        }

        if (!userPasswordUpdateDTO.getNewPassword().equals(userPasswordUpdateDTO.getConfirmPassword())) {
            throw new PasswordEditFailedException("两次输入的新密码不一致");
        }

        if (userPasswordUpdateDTO.getOldPassword().equals(userPasswordUpdateDTO.getNewPassword())) {
            throw new PasswordEditFailedException("新密码不能与旧密码相同");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (user.getStatus() != null && user.getStatus().equals(StatusConstant.DISABLE)) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        String oldPassword = DigestUtils.md5DigestAsHex(userPasswordUpdateDTO.getOldPassword().getBytes());
        if (!oldPassword.equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        user.setPassword(DigestUtils.md5DigestAsHex(userPasswordUpdateDTO.getNewPassword().getBytes()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
