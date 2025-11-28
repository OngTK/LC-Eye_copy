package lceye.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lceye.service.ExcelService;
import lceye.aop.SessionToken;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    /**
     *  [Excel-01] 엑셀 다운로드
     * @param token session에서 token 추출 (by AOP)
     * @param pjno 프로젝트 번호
     * @param response excel 다운로드를 위한 응답
     * @author OngTK
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadExcel(@SessionToken String token,
                                           @RequestParam int pjno,
                                           HttpServletResponse response){
        // [2] service에 엑셀 출력 요청
        boolean result = excelService.downloadExcel(token, pjno, response);

        // [3] 결과 반환
        if(!result) return ResponseEntity.status(403).body("잘못된 요청입니다.");
        return ResponseEntity.ok().build();
    } // func end
} // class end