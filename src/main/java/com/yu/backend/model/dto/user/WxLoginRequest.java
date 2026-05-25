package com.yu.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信小程序登录（wx.login 的 code）
 */
@Data
public class WxLoginRequest implements Serializable {

    /** wx.login 临时 code */
    private String code;

    /** 用户授权后的昵称（可选，需前端 getUserProfile 或头像昵称填写能力） */
    private String nickName;

    /** 用户授权后的头像 URL（可选） */
    private String avatarUrl;
}
