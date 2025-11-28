package lceye.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ExcelProjectDto {

    // [1] 기본정보
    private int pjno;           // 프로젝트 번호
    private String pjname;      // 프로젝트 명
    private String pjdesc;      // 프로젝트 설명
    private double pjamount;    // 생산량
    private String ugname;      // 단위 그룹 이름
    private String unit;        // 상세 단위 이름

    private String mname;       // 작성자 이름
    private String memail;      // 작성자 이메일
    private String cnmae;       // 사명

    private String pjfilename;  // 프로젝트 정보 저장 파일(Process 까지 정보를 담은 json 파일)

    private String updatedate;  // 수정일
    private String createdate;  // 생성일

} // class end
