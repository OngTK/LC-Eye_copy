package lceye.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)         // 메소드에 붙일 수 있는 어노테이션 정의
public @interface DistributedLock {
    // 1. Lock Key 정의
    String lockKey();
    // 2. 시간 단위 정의(초)
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    // 3. 락 대기 시간
    int waitTime() default 10;
    // 4. 락 임대 시간
    int leaseTime() default 5;
} // annotation end