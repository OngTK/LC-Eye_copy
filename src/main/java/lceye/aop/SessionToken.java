package lceye.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)      // 매개변수에 붙일 수 있는 어노테이션 정의
public @interface SessionToken {

} // annotation end