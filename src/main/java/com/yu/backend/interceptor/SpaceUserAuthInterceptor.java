package com.yu.backend.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.yu.backend.context.SpaceUserAuthContextHolder;
import com.yu.backend.exception.BusinessException;
import com.yu.backend.exception.ErrorCode;
import com.yu.backend.filter.RequestWrapper;
import com.yu.backend.manager.SpaceUserAuthManager;
import com.yu.backend.manager.SpaceUserAuthRouteChecker;
import com.yu.backend.model.dto.space.SpaceUserAuthContext;
import com.yu.backend.model.entity.User;
import com.yu.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * 空间成员权限拦截器（JWT 登录态，非 Sa-Token）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpaceUserAuthInterceptor implements HandlerInterceptor {

    private static final String[] SKIP_PATHS = {
            "/user/login",
            "/user/register",
            "/doc.html",
            "/webjars/",
            "/v3/api-docs",
            "/swagger",
            "/wx/mp"
    };

    private final UserService userService;
    private final SpaceUserAuthManager spaceUserAuthManager;
    private final SpaceUserAuthRouteChecker spaceUserAuthRouteChecker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = normalizePath(request);
        if (shouldSkip(path)) {
            return true;
        }

        User loginUser;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (BusinessException e) {
            if (requiresLogin(path)) {
                throw e;
            }
            return true;
        }

        initAuthContextByRequest(request, loginUser);
        checkRoutePermissions(path, loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        SpaceUserAuthContextHolder.clear();
    }

    private void initAuthContextByRequest(HttpServletRequest request, User loginUser) {
        SpaceUserAuthContext authRequest = parseAuthContext(request);
        Long id = authRequest.getId();
        if (Objects.nonNull(id)) {
            String path = normalizePath(request);
            String partUri = path.startsWith("/") ? path.substring(1) : path;
            String moduleName = StrUtil.subBefore(partUri, "/", false);
            switch (moduleName) {
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "file":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                default:
                    break;
            }
        }
        authRequest.setPermissionList(spaceUserAuthManager.getPermissionList(loginUser, authRequest));
        SpaceUserAuthContextHolder.set(loginUser.getId().toString(), authRequest);
    }

    private SpaceUserAuthContext parseAuthContext(HttpServletRequest request) {
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        if (contentType != null && contentType.contains(ContentType.JSON.getValue())) {
            String body;
            if (request instanceof RequestWrapper) {
                body = ((RequestWrapper) request).getBody();
            } else {
                body = ServletUtil.getBody(request);
            }
            if (StrUtil.isNotBlank(body)) {
                return JSONUtil.toBean(body, SpaceUserAuthContext.class);
            }
        }
        Map<String, String> paramMap = ServletUtil.getParamMap(request);
        if (!paramMap.isEmpty()) {
            return BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        return new SpaceUserAuthContext();
    }

    private void checkRoutePermissions(String path, User loginUser) {
        SpaceUserAuthContext authContext = SpaceUserAuthContextHolder.get(loginUser.getId().toString());
        if (path.startsWith("/picture/")) {
            if (path.contains("edit") || path.contains("delete") || path.contains("upload") || path.contains("admin")) {
                spaceUserAuthRouteChecker.checkPictureRoute(path, authContext);
            }
        } else if (path.startsWith("/file/")) {
            if (path.contains("upload")) {
                spaceUserAuthRouteChecker.checkPictureRoute(path, authContext);
            }
        } else if (path.startsWith("/spaceUser/")) {
            if (path.contains("manage")) {
                spaceUserAuthRouteChecker.checkSpaceUserRoute(path, authContext);
            }
        } else if (path.startsWith("/space/")) {
            if (path.contains("manage") || path.contains("delete")) {
                spaceUserAuthRouteChecker.checkSpaceRoute(path, authContext);
            }
        }
    }

    private boolean requiresLogin(String path) {
        if (path.startsWith("/picture/")) {
            return path.contains("edit") || path.contains("delete") || path.contains("upload") || path.contains("admin");
        }
        if (path.startsWith("/file/")) {
            return path.contains("upload");
        }
        if (path.startsWith("/spaceUser/")) {
            return path.contains("manage");
        }
        if (path.startsWith("/space/")) {
            return path.contains("manage") || path.contains("delete") || path.contains("edit");
        }
        return false;
    }

    private boolean shouldSkip(String path) {
        return Arrays.stream(SKIP_PATHS).anyMatch(path::startsWith);
    }

    private String normalizePath(HttpServletRequest request) {
        return normalizePath(request.getRequestURI());
    }

    private String normalizePath(String uri) {
        if (uri == null) {
            return "";
        }
        if (uri.startsWith("/api")) {
            return uri.substring(4);
        }
        return uri;
    }
}
