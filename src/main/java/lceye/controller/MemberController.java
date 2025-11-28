package lceye.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lceye.model.dto.MemberDto;
import lceye.service.MemberService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * [MB-01] 로그인(login)
     * <p>
     * [아이디, 비밀번호]를 받아서 DB에 일치하는 회원이 존재한다면, Redis Session에 로그인 정보가 담긴 JWT 토큰을 저장한다.
     * <p>
     * 테스트 : {"mid":"admin", "mpwd":"1234"}
     * @param memberDto 아이디, 비밀번호가 담긴 Dto
     * @param request 요청한 회원의 HTTP 요청 정보
     * @return 로그인을 성공한 회원의 Dto
     * @author AhnJH
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberDto memberDto, HttpServletRequest request){
        // 1. 입력받은 값을 Service에 전달하여 로그인 진행
        MemberDto result = memberService.login(memberDto);
        // 2. 로그인을 성공했다면
        if (result != null){
            // 3. 세션 및 Redis에 토큰 저장
            HttpSession session = request.getSession(true);
            session.setAttribute("loginMember", result.getToken());
            // 4. 성공한 회원의 Dto 반환
            return ResponseEntity.status(200).body(result);
        } // if end
        // 5. 로그인을 실패했다면, false 반환
        return ResponseEntity.status(401).body(null);
    } // func end

    /**
     * [MB-01] 플러터 로그인(login)
     * <p>
     * [아이디, 비밀번호]를 받아서 DB에 일치하는 회원이 존재한다면, Redis에 로그인 정보가 담긴 JWT 토큰을 저장한다.
     * <p>
     * 테스트 : {"mid":"admin", "mpwd":"1234"}
     * @param memberDto 아이디, 비밀번호가 담긴 Dto
     * @return 로그인을 성공한 회원의 Dto
     * @author AhnJH
     */
    @PostMapping("/flutter/login")
    public ResponseEntity<?> flutterLogin(@RequestBody MemberDto memberDto){
        // 1. 입력받은 값을 Service에 전달하여 로그인 진행
        MemberDto result = memberService.flutterLogin(memberDto);
        // 2. 로그인을 성공했다면
        if (result != null){
            return ResponseEntity.ok(result.getToken());
        } // if end
        // 3. 최종적으로 결과 반환
        return ResponseEntity.status(401).body(null);
    } // func end


    /**
     * [MB-02] 로그아웃(logout)
     * <p>
     * 요청한 회원의 로그인 정보를 Redis Session에서 제거한다.
     * @param request 요청한 회원의 HTTP 요청 정보
     * @return 로그아웃 성공 여부 - boolean
     * @author AhnJH
     */
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        // 1. 세션 전체 비우기 진행
        HttpSession session = request.getSession(false);
        if (session != null){
            session.invalidate();
        } // if end
        // 2. true 반환
        return ResponseEntity.ok(true);
    } // func end

    /**
     * [MB-02] 플러터 로그아웃(logout)
     * <p>
     * 요청한 회원의 로그인 정보를 Redis에서 제거한다.
     * @param authorizationHeader 요청한 회원의 token 정보
     * @return 로그아웃 성공 여부 - boolean
     * @author AhnJH
     */
    @PostMapping("/flutter/logout")
    public ResponseEntity<?> flutterLogout(@RequestHeader("Authorization") String authorizationHeader){
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            String token = authorizationHeader.substring(7);
            // 4. Redis에 저장된 쿠키 삭제 진행 후 반환
            return ResponseEntity.ok(memberService.flutterLogout(token));
        }
        return ResponseEntity.status(401).body(false);
    } // func end

    /**
     * [MB-03] 로그인 정보 확인(getInfo)
     * <p>
     * 요청한 회원의 [로그인 여부, 권한, 회원명, 회사명]을 반환한다.
     * @param request 요청한 회원의 HTTP 요청 정보
     * @return 요청한 회원의 정보
     * @author AhnJH
     */
    @GetMapping("/getinfo")
    public ResponseEntity<?> getInfo(HttpServletRequest request){
        // 1. session에서 토큰 꺼내기
        HttpSession session = request.getSession(false);
        String token = null;
        if (session != null){
            token = (String) session.getAttribute("loginMember");
        } // if end
        // 2. 쿠키 내 토큰이 존재하고
        if (token != null){
            Map<String, Object> infoByToken = memberService.getInfo(token);
            // 3. 해당 토큰이 유효하여 정보 추출에 성공했다면
            if (infoByToken != null){
                // 4. 로그인여부를 표시하고
                infoByToken.put("isAuth", true);
                // 5. HTTP 200으로 반환
                return ResponseEntity.status(200).body(infoByToken);
            } // if end
        } // if end
        // 6. 비로그인 상태라면
        Map<String, Object> result = new HashMap<>();          // infoByToken이 null일 수 있기에, 재정의 해주기
        // 7. 비로그인을 표시하고
        result.put("isAuth", false);
        // 8. HTTP 403으로 반환
        return ResponseEntity.status(403).body(result);
    } // func end

    @GetMapping("/flutter/getinfo")
    public ResponseEntity<?> flutterGetInfo(@RequestHeader("Authorization") String header){
        if (header != null && header.startsWith("Bearer ")){
            String token = header.substring(7);
            Map<String, Object> infoByToken = memberService.getInfo(token);
            // 2. 해당 토큰이 유효하여 정보 추출에 성공했다면
            if (infoByToken != null){
                // 3. 로그인여부를 표시하고
                infoByToken.put("isAuth", true);
                // 4. HTTP 200으로 반환
                return ResponseEntity.status(200).body(infoByToken);
            } // if end
        } // if end
        // 5. 비로그인 상태라면
        Map<String, Object> result = new HashMap<>();          // infoByToken이 null일 수 있기에, 재정의 해주기
        // 6. 비로그인을 표시하고
        result.put("isAuth", false);
        // 7. HTTP 403으로 반환
        return ResponseEntity.status(403).body(result);
    } // func end
} // class end