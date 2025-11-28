package lceye.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lceye.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SessionAspect {
    private final JwtService jwtService;

    /**
     * 메소드 실행 전, Session에서 Token을 추출해서 주입해주는 역할
     *
     * @param joinPoint 메소드 실행제어
     * @return 메소드 실행 및 반환
     * @throws Throwable
     * @author AhnJH
     */
    @Around("execution(* lceye.controller..*(..))")
    public Object injectToken(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 현재 요청에서 세션과 토큰 추출
        // 1.1. 토큰 값을 담을 변수 선언
        String token = null;
        // 1.2. 현재 요청과 연결된 HTTP 속성 정보 가져오기
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 1.3. 속성 정보를 가져오는데 성공했다면
        if (attributes != null){
            // 1.4. 속성 정보에서 HttpServletRequest와 HttpSession 객체 꺼내기
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession(false);
            // 1.5. 세션이 존재한다면
            if (session != null){
                // 1.6. 세션에서 토큰 정보 꺼내서 저장하기
                token = (String) session.getAttribute("loginMember");
            } // if end
        } // if end

        // 2. 토큰 유효성 검사 (Token Validation)
        if (token != null) {
            boolean isValid = jwtService.validateToken(token);

            if (!isValid) {
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            } // if end
        } // if end

        // 3. 컨트롤러 메소드의 매개변수 정보 가져오기
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();

        // 4. 매개변수를 순회하면서 @SessionToken이 붙은 곳 찾기
        for (int i = 0; i < parameters.length; i++){
            // 3.1. SessionToken이 붙은 매개변수를 찾고
            if (parameters[i].isAnnotationPresent(SessionToken.class)){
                // 3.2. 해당 매개변수의 타입이 String이면
                if (parameters[i].getType().equals(String.class)){
                    // 3.3. 해당 매개변수에 토큰 주입
                    args[i] = token;
                } // if end
            } // if end
        } // for end

        // =========================== [시간 측정 시작] ===========================
        long startTime = System.currentTimeMillis();

        // 실제 컨트롤러 메서드 실행
        Object result = joinPoint.proceed(args);

        // 실행 시간 측정 종료 및 로그 출력
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 어떤 컨트롤러의 메서드가 얼마나 걸렸는지 출력 (예: ProjectController.getList)
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        log.info("⏱️ 실행 시간: {} ms | 메서드: {}", duration, methodName);
        // =========================================================

        return result;
    } // func end
} // class end