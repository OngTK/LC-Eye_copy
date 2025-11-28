package lceye.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        // 1. Redis 템플릿 객체 생성 : Redis 형식을 Map 타입으로 사용하기위한 설정
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 2. 생성한 템플릿 객체를 팩토리(Redis 저장소)에 등록
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 3. 생성한 템플릿은 key값을 String 타입으로 직렬화한다.
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 4. 생성한 템플릿은 value값을 JSON/DTO 타입으로 직렬화한다,
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 직렬화 : Redis에 저장된 데이터를 자바 타입으로 변환 과정
        return redisTemplate;
    } // func end
} // class end