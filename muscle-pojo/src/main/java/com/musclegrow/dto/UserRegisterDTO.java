package com.musclegrow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户注册传递的数据模型")
public class UserRegisterDTO implements Serializable {

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("确认密码")
    private String confirmPassword;
}
