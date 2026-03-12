package com.yu.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户更新请求
 * 管理员使用
 */
@Data

public class UserUpdateRequest implements Serializable {


    /**
     * 用户 id
      */
     private Long id;


    /**
     * 用户昵称
     */
    private String userName;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}