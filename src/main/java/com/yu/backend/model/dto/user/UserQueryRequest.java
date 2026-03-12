package com.yu.backend.model.dto.user;

import com.yu.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
//支持分页查询

@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     *用户简介
     */
    private String userProfile;

    /**
     * 用户角色
     *
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
