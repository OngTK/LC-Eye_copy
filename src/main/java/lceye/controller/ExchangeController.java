package lceye.controller;

import lceye.service.ExchangeService;
import lceye.aop.SessionToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController @RequestMapping("/api/inout")
@RequiredArgsConstructor
public class ExchangeController {

    /**
     * 서비스 불러오기
     */
    private final ExchangeService exchangeService;

    //@PostMapping("/auto") // localhost:8080/api/inout/auto
    //public ResponseEntity<?> test(){
    //    String text = "경유";
    //    return ResponseEntity.ok(translationService.Translate(text));
    //}// func end

    /**
     * 투입물·산출물 저장/수정
     *
     * @param token session에서 token 추출 (by AOP)
     * @param map 투입물·산출물 정보
     * @return boolean
     * @author 민성호
     */
    @PostMapping
    public ResponseEntity<?> saveIOInfo(@SessionToken String token,
                                        @RequestBody Map<String,Object> map){
        System.out.println("map : " + map);
        if (token != null){
            Object pjnoObject = map.get("pjno");
            int pjno = 0;
            if (pjnoObject instanceof Number) {
                pjno = ((Number) pjnoObject).intValue();
            } // if end
            return ResponseEntity.ok(exchangeService.saveInfo(map,token, pjno));
        } else {
            return ResponseEntity.status(401).body(null);
        } // if end
    }// func end

    /**
     * 투입물·산출물 프로세스와 자동매칭
     *
     * @param token session에서 token 추출 (by AOP)
     * @param inputList 클라이언트가 입력한 투입물·산출물
     * @return Map<String,Object>
     * @author 민성호
     */
    @PostMapping("/auto")
    public ResponseEntity<?> matchIO(@SessionToken String token,
                                     @RequestBody List<String> inputList){
        if (token != null){
            Map<String, Set<String>> pjnoMap = exchangeService.autoMatchPjno(inputList,token);
            if (pjnoMap != null && !pjnoMap.isEmpty()){
                return ResponseEntity.ok(pjnoMap);
            } else {
                return ResponseEntity.status(404).body(null);
            } // if end
        } else {
            return ResponseEntity.status(404).body(null);
        } // if end
    }// func end

    /**
     * 프로젝트 초기화
     *
     * @param token session에서 token 추출 (by AOP)
     * @param pjno 삭제할 프로젝트번호
     * @return boolean
     * @author 민성호
     */
    @DeleteMapping
    public ResponseEntity<?> clearIOInfo(@SessionToken String token,
                                         @RequestParam int pjno){
        if (token != null){
            return ResponseEntity.ok(exchangeService.clearIOInfo(token,pjno));
        } else {
            return ResponseEntity.status(404).body(null);
        } // if end
    }// func end

    /**
     * [IO-03] 투입물·산출물 정보 조회
     * <p>
     * 프로젝트 번호를 매개변수로 받아 투입물·산출물 json파일에서 exchanges 정보를 반환한다.
     * @param pjno - 조회할 프로젝트 번호
     * @return List<Map<Strig,Object>>
     * @author OngTK
     */
    @GetMapping
    public ResponseEntity<?> readIOInfo(@RequestParam int pjno){
        Map<String, Object> result = exchangeService.readIOInfo(pjno);
        if(result == null){
            return ResponseEntity.status(403).body("잘못된 요청입니다.");
        }
        return ResponseEntity.ok(result);
    } // func end
} // class end