package lceye.controller;

import lceye.service.UnitsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitsController {

    private final UnitsService unitsService;

    /**
     * [UN-01] 단위 조회
     */
    @GetMapping
    public ResponseEntity<?> readAllUnit(){
        return ResponseEntity.ok(unitsService.readAllUnit());
    } // func end
} // class end