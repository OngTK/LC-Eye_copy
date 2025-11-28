package lceye.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Component
public class AopTransaction {

    // Propagation.REQUIRES_NEW : 부모 트랜잭션 유무와 상관없이 무조건 새로운 트랜잭션을 생성합니다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    } // func end
} // class end