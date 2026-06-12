package com.yu.backend.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.yu.backend.config.JwtProperties;
import com.yu.backend.constant.PictureConstant;
import com.yu.backend.constant.SpaceUserPermissionConstant;
import com.yu.backend.constant.UserConstant;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.manager.SpaceUserAuthManager;
import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.SpaceTypeEnum;
import com.yu.backend.service.PictureService;
import com.yu.backend.service.SpaceService;
import com.yu.backend.service.UserService;
import com.yu.backend.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    private JwtProperties jwtProperties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest)) {
            return false;
        }
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String pictureIdStr = servletRequest.getParameter("pictureId");
        String spaceIdStr = servletRequest.getParameter("spaceId");
        if (StrUtil.isBlank(pictureIdStr)) {
            log.error("缺少 pictureId 参数，拒绝握手");
            return false;
        }
        User loginUser = resolveLoginUser(servletRequest);
        if (ObjUtil.isEmpty(loginUser)) {
            log.error("用户未登录，拒绝握手");
            return false;
        }
        long pictureId;
        Long querySpaceId = null;
        try {
            pictureId = Long.parseLong(pictureIdStr.trim());
            if (StrUtil.isNotBlank(spaceIdStr)) {
                querySpaceId = Long.parseLong(spaceIdStr.trim());
            }
        } catch (NumberFormatException e) {
            log.error("pictureId/spaceId 格式错误，拒绝握手");
            return false;
        }
        Picture picture = pictureService.getPicture(pictureId, querySpaceId);
        if (picture == null) {
            log.error("图片不存在，拒绝握手");
            return false;
        }
        Long spaceId = picture.getSpaceId();
        Space space = null;
        if (!PictureConstant.isPublicSpace(spaceId)) {
            space = spaceService.getById(spaceId);
            if (space == null) {
                log.error("空间不存在，拒绝握手");
                return false;
            }
            if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                log.info("不是团队空间，拒绝协同编辑握手");
                return false;
            }
        } else {
            log.info("公共图库不支持协同编辑，拒绝握手");
            return false;
        }
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        if (!SpaceUserAuthManager.hasPermission(permissionList, SpaceUserPermissionConstant.PICTURE_EDIT)) {
            log.error("没有图片编辑权限，拒绝握手");
            return false;
        }
        attributes.put("user", loginUser);
        attributes.put("userId", loginUser.getId());
        attributes.put("pictureId", pictureId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private User resolveLoginUser(HttpServletRequest servletRequest) {
        try {
            return userService.getLoginUser(servletRequest);
        } catch (BusinessException ignored) {
        }
        String token = servletRequest.getParameter("token");
        if (StrUtil.isBlank(token)) {
            return null;
        }
        if (token.startsWith(UserConstant.TOKEN_PREFIX)) {
            token = token.substring(UserConstant.TOKEN_PREFIX.length()).trim();
        }
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            Long userId = JwtUtils.getUserId(token, jwtProperties.getSecret());
            return userService.getById(userId);
        } catch (Exception e) {
            return null;
        }
    }
}
