package com.yu.backend.manager;

import com.yu.backend.constant.SpaceUserPermissionConstant;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.exception.ThrowUtils;
import com.yu.backend.model.dto.space.SpaceUserAuthContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 按路由关键字校验空间权限（替代 Sa-Token 路由匹配）
 */
@Component
public class SpaceUserAuthRouteChecker {

    public void checkPictureRoute(String path, SpaceUserAuthContext authContext) {
        List<Boolean> checks = new ArrayList<>();
        if (path.contains("edit")) {
            checks.add(hasPermission(authContext, SpaceUserPermissionConstant.PICTURE_EDIT));
        }
        if (path.contains("delete")) {
            checks.add(hasPermission(authContext, SpaceUserPermissionConstant.PICTURE_DELETE));
        }
        if (path.contains("upload")) {
            checks.add(hasPermission(authContext, SpaceUserPermissionConstant.PICTURE_UPLOAD));
        }
        if (path.contains("admin")) {
            checks.add(hasPermission(authContext, SpaceUserPermissionConstant.SPACE_USER_MANAGE));
        }
        ThrowUtils.throwIf(checks.contains(Boolean.FALSE), ErrorCode.NO_AUTH_ERROR, "无权限访问该接口");
    }

    public void checkSpaceUserRoute(String path, SpaceUserAuthContext authContext) {
        if (path.contains("manage")) {
            ThrowUtils.throwIf(!hasPermission(authContext, SpaceUserPermissionConstant.SPACE_USER_MANAGE),
                    ErrorCode.NO_AUTH_ERROR, "无权限访问该接口");
        }
    }

    public void checkSpaceRoute(String path, SpaceUserAuthContext authContext) {
        List<Boolean> checks = new ArrayList<>();
        if (path.contains("manage")) {
            checks.add(hasPermission(authContext, SpaceUserPermissionConstant.SPACE_USER_MANAGE));
        }
        if (path.contains("delete")) {
            checks.add(hasPermission(authContext, SpaceUserPermissionConstant.SPACE_USER_MANAGE));
        }
        ThrowUtils.throwIf(checks.contains(Boolean.FALSE), ErrorCode.NO_AUTH_ERROR, "无权限访问该接口");
    }

    private boolean hasPermission(SpaceUserAuthContext authContext, String permission) {
        if (authContext == null || authContext.getPermissionList() == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该接口");
        }
        return SpaceUserAuthManager.hasPermission(authContext.getPermissionList(), permission);
    }
}
