package com.yu.backend.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yu.backend.model.dto.picture.PictureEditRequestMessage;
import com.yu.backend.model.dto.picture.PictureEditResponseMessage;
import com.yu.backend.model.entity.User;
import com.yu.backend.model.enums.PictureEditActionEnum;
import com.yu.backend.model.enums.PictureEditMessageTypeEnum;
import com.yu.backend.model.vo.UserVO;
import com.yu.backend.service.UserService;
import com.yu.backend.websocket.disruptor.PictureEditEventProducer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    private final Map<Long, List<TextMessage>> pictureEditRecodes = new ConcurrentHashMap<>();

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.computeIfAbsent(pictureId, k -> ConcurrentHashMap.newKeySet()).add(session);

        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        responseMessage.setMessage(String.format("%s加入编辑", user.getUserName()));
        responseMessage.setUser(userService.getUserVO(user));
        TextMessage textMessage = getTextMessage(responseMessage);
        broadcastToPicture(pictureId, textMessage);
        broadcastToOneUser(pictureId, pictureEditRecodes.get(pictureId), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        PictureEditRequestMessage requestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureEditEventProducer.publishEvent(requestMessage, session, user, pictureId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            User user = (User) session.getAttributes().get("user");
            Long pictureId = (Long) session.getAttributes().get("pictureId");
            if (user == null || pictureId == null) {
                return;
            }
            PictureEditContext context = new PictureEditContext(
                    new PictureEditRequestMessage(PictureEditMessageTypeEnum.EXIT_EDIT.getValue(), null),
                    session, user, pictureId);
            handleExitEditMessage(context);
        } catch (Exception e) {
            log.error("连接关闭清理失败: {}", ExceptionUtils.getRootCauseMessage(e));
        }
    }

    public void handleEnterEditMessage(PictureEditContext pictureEditContext) throws Exception {
        User user = pictureEditContext.getUser();
        Long pictureId = pictureEditContext.getPictureId();
        if (!pictureEditingUsers.containsKey(pictureId)) {
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            responseMessage.setMessage(String.format("%s开始编辑图片", user.getUserName()));
            responseMessage.setUser(userService.getUserVO(user));
            broadcastToPicture(pictureId, getTextMessage(responseMessage));
        }
        if (!Objects.equals(pictureEditingUsers.get(pictureId), user.getId())) {
            handleEditErrorMessage(user, pictureId, pictureEditContext.getSession());
        }
    }

    private void handleEditErrorMessage(User user, Long pictureId, WebSocketSession sendSession) throws Exception {
        Long editUserId = pictureEditingUsers.get(pictureId);
        User editUser = userService.getById(editUserId);
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
        String editUserName = editUser != null ? editUser.getUserName() : "其他用户";
        responseMessage.setMessage(String.format("操作失败，%s 正在操作", editUserName));
        responseMessage.setUser(userService.getUserVO(user));
        broadcastToOneUser(pictureId, Collections.singletonList(getTextMessage(responseMessage)), sendSession);
    }

    public void handleEditActionMessage(PictureEditContext pictureEditContext) throws Exception {
        PictureEditRequestMessage requestMessage = pictureEditContext.getRequestMessage();
        WebSocketSession session = pictureEditContext.getSession();
        User user = pictureEditContext.getUser();
        Long pictureId = pictureEditContext.getPictureId();
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = requestMessage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (actionEnum == null) {
            return;
        }
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            responseMessage.setMessage(String.format("%s执行%s", user.getUserName(), actionEnum.getText()));
            responseMessage.setEditAction(editAction);
            responseMessage.setUser(userService.getUserVO(user));
            TextMessage textMessage = getTextMessage(responseMessage);
            broadcastToPicture(pictureId, textMessage, session);

            PictureEditResponseMessage saveMessage = new PictureEditResponseMessage();
            saveMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            saveMessage.setEditAction(editAction);
            pictureEditRecodes.computeIfAbsent(pictureId, k -> new ArrayList<>())
                    .add(getTextMessage(saveMessage));
        }
    }

    public void handleExitEditMessage(PictureEditContext pictureEditContext) {
        Long pictureId = null;
        try {
            User user = pictureEditContext.getUser();
            pictureId = pictureEditContext.getPictureId();
            WebSocketSession session = pictureEditContext.getSession();
            Long editingUserId = pictureEditingUsers.get(pictureId);
            if (editingUserId != null && editingUserId.equals(user.getId())) {
                PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
                responseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
                responseMessage.setMessage(String.format("%s退出编辑图片", user.getUserName()));
                responseMessage.setUser(userService.getUserVO(user));
                broadcastToPicture(pictureId, getTextMessage(responseMessage));
                pictureEditingUsers.remove(pictureId);
            }
            Set<WebSocketSession> sessions = pictureSessions.get(pictureId);
            if (sessions != null) {
                sessions.remove(session);
            }
        } catch (Exception e) {
            log.error("handleExitEditMessage 失败: {}", ExceptionUtils.getRootCauseMessage(e));
            throw new RuntimeException(e);
        } finally {
            if (pictureId != null) {
                Set<WebSocketSession> sessions = pictureSessions.get(pictureId);
                if (sessions != null && sessions.isEmpty()) {
                    pictureEditRecodes.remove(pictureId);
                    pictureSessions.remove(pictureId);
                    pictureEditingUsers.remove(pictureId);
                }
            }
        }
    }

    private void broadcastToPicture(Long pictureId, TextMessage textMessage, WebSocketSession excludeSession) throws Exception {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isEmpty(sessionSet)) {
            return;
        }
        for (WebSocketSession session : sessionSet) {
            if (excludeSession != null && excludeSession.equals(session)) {
                continue;
            }
            if (session.isOpen()) {
                session.sendMessage(textMessage);
            }
        }
    }

    private void broadcastToPicture(Long pictureId, TextMessage textMessage) throws Exception {
        broadcastToPicture(pictureId, textMessage, null);
    }

    private void broadcastToOneUser(Long pictureId, List<TextMessage> textMessages, WebSocketSession sendSession) throws Exception {
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isEmpty(sessionSet) || CollUtil.isEmpty(textMessages)) {
            return;
        }
        for (TextMessage textMessage : textMessages) {
            for (WebSocketSession session : sessionSet) {
                if (session.isOpen() && session.equals(sendSession)) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    private TextMessage getTextMessage(PictureEditResponseMessage responseMessage) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);
        return new TextMessage(objectMapper.writeValueAsString(responseMessage));
    }

    @Data
    @AllArgsConstructor
    public static class PictureEditContext {
        private PictureEditRequestMessage requestMessage;
        private WebSocketSession session;
        private User user;
        private Long pictureId;
    }
}
