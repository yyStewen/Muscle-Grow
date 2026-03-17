package com.musclegrow.service;

import com.musclegrow.dto.UserLoginDTO;
import com.musclegrow.dto.UserRegisterDTO;
import com.musclegrow.entity.User;

public interface UserService {

    void register(UserRegisterDTO userRegisterDTO);

    User login(UserLoginDTO userLoginDTO);
}
