package lceye.model.dto;

import lceye.model.entity.ProjectEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    // 1. 기본적인 정보
    private int pjno;           // 프로젝트 번호
    private String pjname;      // 프로젝트 명
    private double pjamount;    // 생산량
    private String pjdesc;      // 프로젝트 설명
    private String pjfilename;  // 프로젝트 정보 저장 파일(Process 까지 정보를 담은 json 파일)
    private int mno;            // 작성자
    private int uno;            // 상세 단위 번호
    private String createdate;  // 생성일
    private String updatedate;  // 수정일


    // 2. 부가적인 정보
    private String mname;       // 작성자 이름
    private int ugno;        // 단위 그룹 번호
    private String ugname;      // 단위 그룹 이름
    private String unit;       // 상세 단위 이름

    // 3. toEntity 생성
    public ProjectEntity toEntity(){
        return ProjectEntity.builder()
                .pjno(this.pjno)
                .pjname(this.pjname)
                .pjamount(this.pjamount)
                .pjdesc(this.pjdesc)
                .pjfilename(this.pjfilename)
                .build();
    } // func end
} // class end