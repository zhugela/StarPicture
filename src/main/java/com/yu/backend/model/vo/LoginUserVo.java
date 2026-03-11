package com.yu.backend.model.vo;  // 注意包名，放到 vo 包下

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class LoginUserVo implements Serializable {

    private Long id;
    private String userAccount;
    private String userName;
    private String userAvatar;
    private String userProfile;
    private String userRole;
    private Date createTime;

    private static final long serialVersionUID = 1L;
}