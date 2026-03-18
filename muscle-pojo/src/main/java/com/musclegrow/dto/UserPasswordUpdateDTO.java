package com.musclegrow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户修改密码时传递的数据模型")
public class UserPasswordUpdateDTO implements Serializable {

    @ApiModelProperty("旧密码")
    private String oldPassword;

    @ApiModelProperty("新密码")
    private String newPassword;

    @ApiModelProperty("确认新密码")
    private String confirmPassword;
}
