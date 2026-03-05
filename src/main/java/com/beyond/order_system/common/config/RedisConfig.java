package com.beyond.order_system.common.config;

import com.beyond.order_system.common.service.SseAlarmService;
import com.fasterxml.jackson.databind.ser.impl.StringArraySerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

//    연결 빈 객체
    @Bean
//    Qualifier : 같은 Bean 객체가 여러개 있을 경우 Bean객체를 구분하기 위한 어노테이션
//    현재 트렌드는 0번 DB만 사용, Redis 컨테이너를 여러 개 띄움.
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }

    //    탬플릿 빈 객체
    @Bean
    @Qualifier("rtInventory")
//    모든 template 중에 무조건 redisTemplate                                                          라는 메서드명이 반드시 1개는 있어야함
//   bean객체 생성 시, bean 객체간에 DI(의존성주입)는 "메서드 파라미터 주입"이 가능
    public RedisTemplate<String, String> redisTemplate(
            @Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    @Qualifier("stockInventory")
    public RedisConnectionFactory stockConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("stockInventory")
    public RedisTemplate<String, String> stockRedisTemplate(
            @Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

//    1. 각 서버가 subscribe
//    2. 메시지를 publish
//    3. 메시지를 받아서 admin@naver.com emitter객체 있는지 여부
//    4. admin@naver.com에게 send
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory ssePubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
//        redis pub/sub기능은 db에 값을 저장하는 기능이 아니므로, 특정 db에 의존적이지 않음.
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> ssePubSubRedisTemplate(
            @Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

//    redis 리스너(subscribe) 객체
//    RedisMessageMessageListener -> MessageListenerAdapter -> SseAlarmService(MessageListener를 구현한)
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory, @Qualifier("ssePubSub")MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
//        만약에 여러 채널을 구독해야 하는 경우, 여러개의 PatternTopic을 add하거나, 별도의 Listener Bean객체 생성
        return container;
    }

//    redis에서 수신된 메시지를 처리하는 객체
    @Bean
    @Qualifier("ssePubSub")
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService) {
//        채널로부터　수신되는 message 처리를 SseAlarmService클래스의 onMessage 메서드로 위임
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }

}
