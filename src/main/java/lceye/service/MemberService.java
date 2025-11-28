package lceye.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lceye.model.dto.MemberDto;
import lceye.model.entity.CompanyEntity;
import lceye.model.entity.MemberEntity;
import lceye.model.mapper.MemberMapper;
import lceye.model.repository.CompanyRepository;
import lceye.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final MemberMapper memberMapper;
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> memberTemplate;

    /**
     * [MB-01] 로그인(login)
     * <p>
     * [아이디, 비밀번호]를 받아서 DB에 일치하는 회원이 존재한다면, Redis와 Cookie에 로그인 정보가 담긴 JWT 토큰을 저장한다.
     * </p>
     * @param memberDto 아이디, 비밀번호가 담긴 Dto
     * @return 로그인을 성공한 회원의 Dto
     * @author AhnJH
     */
    public MemberDto login(MemberDto memberDto){
        // 1. 요청된 아이디와 비밀번호가 유효한지 확인
        MemberDto result = memberMapper.login(memberDto);
        // 2. 유효하지 않으면, null 반환
        if (result == null) return null;
        // 3. 유효하면, 로그인정보로 토큰 발급 : loginMno, loginCname, loginRole
        String token = jwtService.generateToken(result);
        result.setToken(token);
        // 4. 최종적으로 토큰이 담긴 Dto 반환
        return result;
    } // func end

    /**
     * [MB-01] 플러터 로그인(login)
     * <p>
     * [아이디, 비밀번호]를 받아서 DB에 일치하는 회원이 존재한다면, Redis와 Cookie에 로그인 정보가 담긴 JWT 토큰을 저장한다.
     * </p>
     * @param memberDto 아이디, 비밀번호가 담긴 Dto
     * @return 로그인을 성공한 회원의 Dto
     * @author AhnJH
     */
    public MemberDto flutterLogin(MemberDto memberDto){
        // 1. 요청된 아이디와 비밀번호가 유효한지 확인
        MemberDto result = memberMapper.login(memberDto);
        // 2. 유효하지 않으면, null 반환
        if (result == null) return null;
        // 3. 유효하면, 로그인정보로 토큰 발급 : loginMno, loginCname, loginRole
        String token = jwtService.generateToken(result);
        result.setToken(token);
        // 4. 발급받은 토큰을 Redis에 저장 : 토큰의 유효시간이 1시간이기에 Redis에도 1시간 적용
        String key = "member:" + result.getMno();     // member:10001
        memberTemplate.opsForValue().set(key, token, Duration.ofHours(1));
        // 5. 최종적으로 토큰이 담긴 Dto 반환
        return result;
    } // func end

    /**
     * [MB-02] 플러터 로그아웃(logout)
     * <p>
     * 요청한 회원의 로그인 정보를 Redis에서 제거한다.
     * @param token 요청한 회원의 token 정보
     * @return Redis 제거 성공 여부 - boolean
     * @author AhnJH
     */
    public boolean flutterLogout(String token){
        // 0. 토큰이 유효한지 확인
        if (!jwtService.validateToken(token)) return false;
        // 1. 토큰으로부터 요청한 회원번호 추출하기
        int loginMno = jwtService.getMnoFromClaims(token);
        // 2. 회원번호를 토대로 토큰 key 생성
        String key = "member:" + loginMno;
        // 3. 요청한 로그인정보를 Redis에서 제거
        return memberTemplate.delete(key);
    } // func end

    /**
     * [MB-03] 로그인 정보 확인(getInfo)
     * <p>
     * 요청한 회원의 [로그인 여부, 권한, 회원명, 회사명]을 반환한다.
     * @param token 요청한 회원의 token 정보
     * @return 요청한 회원의 정보
     * @author AhnJH
     */
    public Map<String, Object> getInfo(String token){
        // 0. 토큰이 유효한지 확인
        if (!jwtService.validateToken(token)) return null;
        // 1. 토큰으로부터 요청한 회원번호 추출하기 : Redis 검증을 위하여 먼저 추출
        int mno = jwtService.getMnoFromClaims(token);
        // 2. 토큰으로부터 권한과 회사명 추출하기
        String role = jwtService.getRoleFromClaims(token);
        int cno = jwtService.getCnoFromClaims(token);
        // 3. 회원번호로 회원명, 회사번호 추출하기
        Optional<MemberEntity> memberEntity = memberRepository.findById(mno);
        Optional<CompanyEntity> companyEntity = companyRepository.findById(cno);
        String mname = null;
        String cname = null;
        if (memberEntity.isPresent()) mname = memberEntity.get().getMname();
        if (companyEntity.isPresent()) cname = companyEntity.get().getCname();
        // 4. 추출한 정보를 Map 형식으로 변환하기
        Map<String, Object> infoByToken = new HashMap<>();
        infoByToken.put("mno", mno);
        infoByToken.put("mname", mname);
        infoByToken.put("role", role);
        infoByToken.put("cname", cname);
        infoByToken.put("cno", cno);
        // 5. 변환한 Map 반환하기
        return infoByToken;
    } // func end
} // class end