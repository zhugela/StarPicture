package com.yu.backend.model.dto.space;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 空间成员权限配置（对应 biz/spaceUserAuthConfig.json）
 */
@Data
public class SpaceUserAuthConfig implements Serializable {

    private List<SpaceUserPermission> permissions;

    private List<SpaceUserRole> roles;

    private static final long serialVersionUID = 1L;
}
