package lceye.controller;

import lceye.service.LCICalculateService;
import lceye.aop.SessionToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lci")
@RequiredArgsConstructor
public class LCICalculateController {

    private final LCICalculateService lciCalculateService;

    /**
     * [LCI-01] LCI 계산하기
     * <p>
     * 관련 로직은 노션 참고
     * <p>
     * <a href="https://lceye.notion.site/2-6-LCI-2ae094d4983480e0a2efdb327c9e5359?source=copy_link">notion</a>
     * @author OngTK
     */
    @GetMapping("/calc")
    public ResponseEntity<?> calcLCI(@SessionToken String token,
                                     @RequestParam int pjno){
        if (token != null){
            return ResponseEntity.ok(lciCalculateService.calcLCI(pjno, token));
        } else {
            return ResponseEntity.status(401).body(null);
        } // if end
    } // func end

    /**
     * [LCI-02] LCI 결과 조회하기
     * @author OngTK
     */
    @GetMapping
    public ResponseEntity<?> readLCI(@RequestParam int pjno){
        Map<String, Object> result = lciCalculateService.readLCI(pjno);
        if(result == null ){
            return ResponseEntity.status(403).body("잘못된 요청입니다.");
        }
        return ResponseEntity.ok(result);
    } // func end

    /**
     * [LCI-03] LCI 결과 존재 여부 확인
     * @author OngTK
     */
    @GetMapping("/id")
    public ResponseEntity<?> checkLCI(@RequestParam int pjno){
        return ResponseEntity.ok(lciCalculateService.checkLCI(pjno));
    } // func end
} // class end