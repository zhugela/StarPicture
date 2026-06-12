package com.yu.backend.websocket.disruptor;

import com.yu.backend.model.dto.picture.PictureEditRequestMessage;
import com.yu.backend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

@Data
public class PictureEditEvent {

    private PictureEditRequestMessage pictureEditRequestMessage;

    private WebSocketSession session;

    private User user;

    private Long pictureId;
}
