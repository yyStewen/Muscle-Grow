package com.musclegrow.controller.user;

import com.musclegrow.constant.JwtClaimsConstant;
import com.musclegrow.context.BaseContext;
import com.musclegrow.dto.UserLoginDTO;
import com.musclegrow.dto.UserPasswordUpdateDTO;
import com.musclegrow.dto.UserRegisterDTO;
import com.musclegrow.entity.User;
import com.musclegrow.properties.JwtProperties;
import com.musclegrow.result.Result;
import com.musclegrow.service.UserService;
import com.musclegrow.utils.JwtUtil;
import com.musclegrow.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "User auth API")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/register")
    @ApiOperation("User register")
    public Result<String> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("user register, phone={}", userRegisterDTO.getPhone());
        userService.register(userRegisterDTO);
        return Result.success();
    }

    @PostMapping("/login")
    @ApiOperation("User login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("user login, phone={}", userLoginDTO.getPhone());

        User user = userService.login(userLoginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.PHONE, user.getPhone());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }

    @PostMapping("/logout")
    @ApiOperation("User logout")
    public Result<String> logout() {
        return Result.success();
    }

    @PutMapping("/password")
    @ApiOperation("User update password")
    public Result<String> updatePassword(@RequestBody UserPasswordUpdateDTO userPasswordUpdateDTO) {
        log.info("user update password, userId={}", BaseContext.getCurrentId());
        userService.updatePassword(userPasswordUpdateDTO);
        return Result.success();
    }
}
