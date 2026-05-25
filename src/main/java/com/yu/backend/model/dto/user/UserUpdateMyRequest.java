package com.yu.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户更新自己的资料
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    private String userName;

    private String userAvatar;

    private String userProfile;

    private static final long serialVersionUID = 1L;
}
