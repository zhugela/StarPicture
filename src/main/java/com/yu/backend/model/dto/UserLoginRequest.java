package com.yu.backend.model.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserLoginRequest implements Serializable {
    /**
     * 账号
     */
    private String userAccount;
    /**
     * 密码
     */
    private String userPassword;
    /*
        * 序列号
     */
    private static final long serialVersionUID = 1L;
}
