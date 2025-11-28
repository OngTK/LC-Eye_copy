package lceye.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lceye.model.dto.MemberDto;

@Service
public class JwtService {
    private final String secret = "LCEyeSecretKeyIsVeryDifficultSecretKey";
    // 비밀키를 기반으로 SHA-256 알고리즘 적용
    private final Key secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    /**
     * 로그인한 회원의 mno, cname, mrole을 바탕으로 토큰을 생성해주는 메소드
     * @param memberDto mno, cname, mrole이 들어있는 Dto
     * @return 생성된 토큰
     * @author AhnJH
     */
    public String generateToken(MemberDto memberDto){
        // 1. 토큰 객체 빌더 시작
        String token = Jwts.builder()
                // 2. loginMno에 mno 저장
                .claim("loginMno", memberDto.getMno())
                // 3. loginCno에 cno 저장
                .claim("loginCno", memberDto.getCno())
                // 4. loginRole에 mrole 저장
                .claim("loginRole", memberDto.getMrole())
                // 5. 토큰 발급시간에 현재 시간 저장
                .setIssuedAt(new Date())
                // 6. 만료 시간을 1시간으로 설정
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                // 7. HS256 알고리즘을 이용하여 서명
                .signWith(secretKey, SignatureAlgorithm.HS256)
                // 8. 토큰 객체 빌더 종료
                .compact();
        // 9. 최종적으로 토큰 반환
        return token;
    } // func end

    /**
     * 해당 토큰이 유효한지 확인하는 메소드
     * @param token 유효성을 확인할 토큰
     * @return 유효(true)/무효(false)
     * @author AhnJH
     */
    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    // 1. 검증을 위한 비밀키 대입
                    .setSigningKey(secretKey)
                    .build()
                    // 2. 검증할 토큰을 대입하여 검증 실행
                    .parseClaimsJws(token);
            // 3. 예외가 발생하지 않으면, 유효
            return true;
        } catch (JwtException e) {
            // 4. 예외가 발생하면, 무효
            return false;
        } // try-catch end
    } // func end

    /**
     * 특정한 토큰의 Claims를 추출하기 위한 메소드
     * @param token Claims를 추출할 토큰
     * @return 추출한 Claims
     * @author AhnJH
     */
    public Claims getClaimsFromToken(String token){
        return Jwts.parser()
                // 1. 검증을 위한 비밀키 대입
                .setSigningKey(secretKey)
                .build()
                // 2. 검증에 성공한 토큰의 Claims 반환
                .parseClaimsJws(token).getBody();
    } // func end

    // Claims의 특정값 추출
    public int getMnoFromClaims(String token){
        return getClaimsFromToken(token).get("loginMno", Integer.class);
    } // func end
    public String getRoleFromClaims(String token){
        return getClaimsFromToken(token).get("loginRole", String.class);
    } // func end
    public int getCnoFromClaims(String token){
        return getClaimsFromToken(token).get("loginCno", Integer.class);
    } // func end
} // class end