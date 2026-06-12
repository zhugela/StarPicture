package com.yu.backend.websocket.disruptor;

import cn.hutool.json.JSONUtil;
import com.yu.backend.model.dto.picture.PictureEditRequestMessage;
import com.yu.backend.model.dto.picture.PictureEditResponseMessage;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.PictureEditMessageTypeEnum;
import com.yu.backend.service.UserService;
import com.yu.backend.websocket.PictureEditHandler;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

@Slf4j
@Component
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {

    @Resource
    @Lazy
    private PictureEditHandler pictureEditHandler;

    @Resource
    private UserService userService;

    @Override
    public void onEvent(PictureEditEvent event) throws Exception {
        PictureEditRequestMessage requestMessage = event.getPictureEditRequestMessage();
        WebSocketSession session = event.getSession();
        User user = event.getUser();
        Long pictureId = event.getPictureId();
        PictureEditMessageTypeEnum messageType = PictureEditMessageTypeEnum.getEnumByValue(requestMessage.getType());
        if (messageType == null) {
            sendError(session, user, "消息类型错误");
            return;
        }
        PictureEditHandler.PictureEditContext context =
                new PictureEditHandler.PictureEditContext(requestMessage, session, user, pictureId);
        switch (messageType) {
            case ENTER_EDIT:
                pictureEditHandler.handleEnterEditMessage(context);
                break;
            case EDIT_ACTION:
                pictureEditHandler.handleEditActionMessage(context);
                break;
            case EXIT_EDIT:
                pictureEditHandler.handleExitEditMessage(context);
                break;
            default:
                sendError(session, user, "消息类型错误");
        }
    }

    private void sendError(WebSocketSession session, User user, String message) throws Exception {
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
        responseMessage.setMessage(message);
        responseMessage.setUser(userService.getUserVO(user));
        session.sendMessage(new TextMessage(JSONUtil.toJsonStr(responseMessage)));
    }
}
