package com.yu.backend.model.dto.picture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditRequestMessage {

    /**
     * 消息类型：ENTER_EDIT / EXIT_EDIT / EDIT_ACTION
     */
    private String type;

    /**
     * 编辑动作：ZOOM_IN / ZOOM_OUT / ROTATE_LEFT / ROTATE_RIGHT
     */
    private String editAction;
}
