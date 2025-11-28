package lceye.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LCICalcLoggingAspect {

    /**
     * LCI Service 메소드 인입 및 결과에 대한 로깅 메소드
     */
    @Around("execution(* lceye.service.LCICalculateService.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        log.info("[LCI-START] {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[LCI-END] {}.{} took {} ms, result={}", className, methodName, elapsed, summarize(result));
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("[LCI-ERROR] {}.{} failed after {} ms: {}", className, methodName, elapsed, ex.getMessage());
            throw ex;
        }
    }

    private String summarize(Object value) {
        if (value == null) {
            return "null";
        }
        String text = value.toString();
        int maxLength = 200;
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        }
        return text;
    }
}
