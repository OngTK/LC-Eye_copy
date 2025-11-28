package lceye.controller;

import lceye.model.dto.ProjectDto;
import lceye.service.ProjectService;
import lceye.aop.SessionToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * [PJ-01] 프로젝트 등록
     * <p>
     * "프로젝트명, 기준양, 기준단위(uno), 프로젝트 설명"을 받아 저장한다.
     * <p>
     * - 참고: 작성자(mno)를 Redis에서 확인하여 함께 저장
     * @author OngTK
     */
    @PostMapping
    public ResponseEntity<?> saveProject(@SessionToken String token,
                                         @RequestBody ProjectDto projectDto){
        ProjectDto result;
        // [1.1] 쿠키 내 토큰이 존재
        if(token!=null){
            result = projectService.saveProject(token,projectDto);
        } else {
            return ResponseEntity.status(403).body("로그인 토큰이 존재하지 않습니다.");
        }

        if( result.getPjno() >= 1){
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(402).body("프로젝트 저장을 실패했습니다.");
        }
    } // func end

    /**
     * [PJ-02] 프로젝트 전체조회
     * <p>
     * 프로젝트를 전체 조회한다.
     * <p>
     * - 참고 : 권한을 확인하여 Manager, Admin인 경우, 작성자와 상관없이 해당 회사의 모든 프로젝트를 조회한다.
     * <p>
     * 권한이 Worker일 경우, 본인이 작성한 프로젝트만 조회한다.
     * @author OngTK
     */
    @GetMapping("/all")
    public ResponseEntity<?> readAllProject(@SessionToken String token){
        if (token != null){
            return ResponseEntity.ok(projectService.readAllProject(token));
        } else {
            return ResponseEntity.status(404).body(null);
        } // if end
    } // func end

    // 플러터용 전체조회
    @GetMapping("/flutter/all")
    public ResponseEntity<?> flutterReadAllProject(@RequestHeader(value = "Authorization" ,required = false) String header){
        System.out.println("ProjectController.flutterReadAllProject");
        System.out.println("header = " + header);
        if (header != null && header.startsWith("Bearer ")){
            String token = header.substring(7);
            return ResponseEntity.ok(projectService.readAllProject(token));
        }
        return ResponseEntity.status(404).body(null);
    } // func end

    /**
     * [PJ-03] 프로젝트 개별조회
     * @author OngTK
     * <p>
     * 권한이 Manager, Admin인 경우 동일한 회사의 타직원이 작성한 프로젝트 정보를 조회할 수 있다.
     * <p>
     * 권한이 Worker인 실무자는 본인의 프로젝트 정보만 조회할 수 있다. 동일한 회사라도 다른 계정에서 작성한 프로젝트 정보는 조회할 수 없다.
     * @quthor OngTK
     */
    @GetMapping
    public ResponseEntity<?> readProject(@SessionToken String token,
                                         @RequestParam int pjno){
        if (token != null){
            return ResponseEntity.ok(projectService.readProject(token, pjno));
        } else {
            return ResponseEntity.status(404).body(null);
        } // if end
    } // func end

    // 플러터용 개별조회
    @GetMapping("/flutter")
    public ResponseEntity<?> flutterReadProject(@RequestHeader(value = "Authorization" ,required = false) String header,
                                                @RequestParam int pjno){
        if (header != null && header.startsWith("Bearer ")){
            String token = header.substring(7);
            return ResponseEntity.ok(projectService.readProject(token,pjno));
        }// if end
        return ResponseEntity.status(404).body(null);
    } // func end

    /**
     * [PJ-04] 프로젝트 수정
     * <p>
     * [프로젝트번호, 프로젝트명, 기준양, 기준단위, 프로젝트 설명] 정보를 받아 수정한다.
     * @author OngTK
     */
    @PutMapping
    public ResponseEntity<?> updateProject(@SessionToken String token,
                                           @RequestBody ProjectDto projectDto){
        ProjectDto result = null;
        if (token != null){
            int pjno = projectDto.getPjno();
            result = projectService.updateProject(token, projectDto, pjno);
        } // if end
        if(result == null){
            return ResponseEntity.status(403).body("잘못된 요청입니다.");
        }
        return ResponseEntity.ok(result);
    }
} // class end