package com.mp.flashsale.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. Cấu hình ObjectMapper để Jackson hiểu LocalDateTime
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Đăng ký JSR310
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Lưu dạng ISO-8601 string

        // 2. Cấu hình "Typing" để Redis biết đây là Class nào khi đọc ra
        // (Tránh việc khi get từ Redis nó trả về LinkedHashMap thay vì Object Item)
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        // 3. Thiết lập Serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }

//    @Bean
//    public RedisMessageListenerContainer redisMessageListenerContainer(
//            RedisConnectionFactory connectionFactory, RedisPendingDepositExpiredListener listener) {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.addMessageListener(listener, new PatternTopic("__keyevent@*__:expired"));
//        return container;
//    }

}
