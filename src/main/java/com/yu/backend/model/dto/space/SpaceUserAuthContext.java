package com.yu.backend.model.dto.space;

import com.yu.backend.model.entity.Picture;
import com.yu.backend.model.entity.Space;
import com.yu.backend.model.entity.SpaceUser;
import lombok.Data;

import java.util.List;

/**
 * 用户在特定空间内的授权上下文
 */
@Data
public class SpaceUserAuthContext {

    private Long id;

    private List<String> permissionList;

    private Long pictureId;

    private Long spaceId;

    private Long spaceUserId;

    private Picture picture;

    private Space space;

    private SpaceUser spaceUser;
}
