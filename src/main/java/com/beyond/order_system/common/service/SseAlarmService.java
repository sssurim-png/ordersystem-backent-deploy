package com.beyond.order_system.common.service;

import com.beyond.order_system.common.dtos.SseMessageDto;
import com.beyond.order_system.common.repository.SseEmitterRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener {
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, ObjectMapper objectMapper, @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void sendMessage(String receiver, String sender, String message) {
        SseMessageDto dto = SseMessageDto.builder()
                .receiver(receiver)
                .sender(sender)
                .message(message)
                .build();
        try {
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);
            String data = objectMapper.writeValueAsString(dto);
//            만약에 emitter객체가 현재 서버에 있으면,바로 알림 발송, 그렇지 않으먄 redis pub/sub활용
            if (sseEmitter != null) {
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
//                사용자가 새로고침 후에 알림메시지를 조회하려면, DB에 추가적으로 저장 필요.
            } else {
//              redis pub sub기능을 활용하여 메시지 publish
                redisTemplate.convertAndSend("order-channel", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
//        message : 실질적으로 메시지가 담겨있는 객체
//        pattern : 채널명
//        추후 여러개의 채널에 각기 다른 메시지를 publish하고, subscribe할 경우, 채널명으로 분기처리 가능.
        String channelName = new String(pattern);

        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());
            String data = objectMapper.writeValueAsString(dto);
//            해당 서버에 receiver의 emitter객체가 있으면 send
            if (sseEmitter != null) {
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
