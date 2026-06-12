package com.yu.backend.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.yu.backend.websocket.disruptor.PictureEditEvent;
import com.yu.backend.websocket.disruptor.PictureEditEventWorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class PictureEditEventDisruptorConfig {

    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> pictureEditEventDisruptor() {
        int bufferSize = 1024 * 256;
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor-").build()
        );
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        disruptor.start();
        return disruptor;
    }
}
