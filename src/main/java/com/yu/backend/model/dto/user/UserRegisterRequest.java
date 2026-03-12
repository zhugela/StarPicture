package com.yu.backend.model.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequest {
    /*
    用户账号
     */
    private String userAccount;
    /*
    用户密码
     */
    private String userPassword;
    /*
    确认密码
     */
    private String checkPassword;

}
