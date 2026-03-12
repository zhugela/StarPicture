package com.yu.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAddRequest implements Serializable {
    /*
      *
      *  用户名
     */
    private String username;
    /*
      *
      *  账号
     */
    private String userAccount;
    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 用户简介
     */
    private String userProfile;
    /**
     * 用户角色
     */
    private String userRole;
    /**
     * 序列号
     */
    private static final long serialVersionUID = 1L;
}
