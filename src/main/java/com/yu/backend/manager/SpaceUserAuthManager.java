package com.yu.backend.manager;



import cn.hutool.core.io.resource.ResourceUtil;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.util.ReflectUtil;

import cn.hutool.core.util.StrUtil;

import cn.hutool.json.JSONUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yu.backend.constant.PictureConstant;
import com.yu.backend.constant.SpaceUserPermissionConstant;

import com.yu.backend.exception.BusinessException;

import com.yu.backend.exception.ErrorCode;

import com.yu.backend.exception.ThrowUtils;

import com.yu.backend.mapper.PictureMapper;

import com.yu.backend.mapper.SpaceMapper;

import com.yu.backend.mapper.SpaceUserMapper;

import com.yu.backend.model.dto.space.SpaceUserAuthConfig;

import com.yu.backend.model.dto.space.SpaceUserAuthContext;

import com.yu.backend.model.dto.space.SpaceUserRole;

import com.yu.backend.model.entity.Picture;

import com.yu.backend.model.entity.Space;

import com.yu.backend.model.entity.SpaceUser;

import com.yu.backend.model.entity.User;

import com.yu.backend.model.enums.SpaceRoleEnum;

import com.yu.backend.model.enums.SpaceTypeEnum;

import com.yu.backend.service.UserService;

import org.springframework.stereotype.Component;



import javax.annotation.Resource;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.Collections;

import java.util.List;



/**

 * 空间成员角色与权限配置（读取 biz/spaceUserAuthConfig.json）

 */

@Component

public class SpaceUserAuthManager {



    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;



    static {

        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");

        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);

    }



    @Resource

    private UserService userService;



    @Resource

    private PictureMapper pictureMapper;



    @Resource

    private SpaceMapper spaceMapper;



    @Resource

    private SpaceUserMapper spaceUserMapper;



    /**

     * 根据角色 key 获取权限列表

     */

    public List<String> getPermissionsByRole(String spaceUserRole) {

        if (StrUtil.isBlank(spaceUserRole) || SPACE_USER_AUTH_CONFIG.getRoles() == null) {

            return new ArrayList<>();

        }

        return SPACE_USER_AUTH_CONFIG.getRoles().stream()

                .filter(r -> spaceUserRole.equals(r.getKey()))

                .findFirst()

                .map(SpaceUserRole::getPermissions)

                .orElseGet(ArrayList::new);

    }



    public static boolean hasPermission(List<String> permissionList, String permission) {

        return permissionList != null && permissionList.contains(permission);

    }



    /**

     * 根据空间与登录用户获取权限列表

     */

    public List<String> getPermissionList(Space space, User loginUser) {

        if (loginUser == null) {

            return new ArrayList<>();

        }

        List<String> adminPermissions = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());

        if (space == null) {

            if (userService.isAdmin(loginUser)) {

                return adminPermissions;

            }

            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);

        }

        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());

        if (spaceTypeEnum == null) {

            return new ArrayList<>();

        }

        switch (spaceTypeEnum) {

            case PRIVATE:

                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {

                    return adminPermissions;

                }

                return new ArrayList<>();

            case TEAM:

                SpaceUser spaceUser = spaceUserMapper.selectOne(

                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SpaceUser>()

                                .eq("spaceId", space.getId())

                                .eq("userId", loginUser.getId()));

                if (spaceUser == null) {

                    return userService.isAdmin(loginUser) ? adminPermissions : new ArrayList<>();

                }

                return getPermissionsByRole(spaceUser.getSpaceRole());

            default:

                return new ArrayList<>();

        }

    }



    /**

     * 根据请求上下文解析权限列表

     */

    public List<String> getPermissionList(User loginUser, SpaceUserAuthContext authContext) {

        List<String> adminPermissions = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());

        if (authContext == null || isAllFieldsNull(authContext)) {

            return adminPermissions;

        }

        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "未找到用户信息");

        Long userId = loginUser.getId();



        SpaceUser spaceUser = authContext.getSpaceUser();

        if (spaceUser != null) {

            return getPermissionsByRole(spaceUser.getSpaceRole());

        }



        Long spaceUserId = authContext.getSpaceUserId();

        if (spaceUserId != null) {

            spaceUser = spaceUserMapper.selectById(spaceUserId);

            ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");

            SpaceUser loginSpaceUser = spaceUserMapper.selectOne(

                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SpaceUser>()

                            .eq("spaceId", spaceUser.getSpaceId())

                            .eq("userId", userId));

            if (loginSpaceUser == null) {

                return userService.isAdmin(loginUser) ? adminPermissions : new ArrayList<>();

            }

            return getPermissionsByRole(loginSpaceUser.getSpaceRole());

        }



        Long spaceId = authContext.getSpaceId();

        if (spaceId == null) {

            Long pictureId = authContext.getPictureId();

            if (pictureId == null) {

                return adminPermissions;

            }

            Picture picture = pictureMapper.selectOne(new QueryWrapper<Picture>().eq("id", pictureId));

            ThrowUtils.throwIf(picture == null, new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息"));

            spaceId = picture.getSpaceId();

            if (PictureConstant.isPublicSpace(spaceId)) {

                if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)) {

                    return adminPermissions;

                }

                return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);

            }

        }



        Space space = spaceMapper.selectById(spaceId);

        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");

        return getPermissionList(space, loginUser);

    }



    private boolean isAllFieldsNull(Object object) {

        if (object == null) {

            return true;

        }

        return Arrays.stream(ReflectUtil.getFields(object.getClass()))

                .map(field -> ReflectUtil.getFieldValue(object, field))

                .allMatch(ObjectUtil::isEmpty);

    }

}


