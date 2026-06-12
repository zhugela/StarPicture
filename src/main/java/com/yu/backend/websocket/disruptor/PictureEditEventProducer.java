package com.yu.backend.websocket.disruptor;

import com.yu.backend.model.dto.picture.PictureEditRequestMessage;
import com.yu.backend.model.entity.User;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Slf4j
@Component
public class PictureEditEventProducer {

    @Resource
    @Qualifier("pictureEditEventDisruptor")
    private Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    public void publishEvent(PictureEditRequestMessage requestMessage, WebSocketSession session,
                             User user, Long pictureId) {
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        long next = ringBuffer.next();
        try {
            PictureEditEvent event = ringBuffer.get(next);
            event.setSession(session);
            event.setPictureEditRequestMessage(requestMessage);
            event.setUser(user);
            event.setPictureId(pictureId);
        } finally {
            ringBuffer.publish(next);
        }
    }

    @PreDestroy
    public void close() {
        pictureEditEventDisruptor.shutdown();
    }
}
