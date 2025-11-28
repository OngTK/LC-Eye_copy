package lceye.model.dto;

import lceye.model.entity.ProjectResultFileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResultFileDto {
    // 1. 기본적인 정보
    private int prfno;
    private String prfname;
    private int pjno;
    private String createdate;
    private String updatedate;

    // 2. 부가적인 정보

    // 3. toEntity 생성
    public ProjectResultFileEntity toEntity(){
        return ProjectResultFileEntity.builder()
                .prfno(this.prfno)
                .prfname(this.prfname)
                .build();
    } // func end
} // class end