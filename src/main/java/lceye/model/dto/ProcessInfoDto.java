package lceye.model.dto;

import lceye.model.entity.ProcessInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInfoDto {
    // 1. 기본적인 정보
    private int pcno;
    private String pcuuid;
    private String pcname;
    private String pcdesc;
    private String pcsource;
//    private String pcfilename; // OngTK 비활성화
    private String createdate;
    private String updatedate;

    // 2. 부가적인 정보

    // 3. toEntity 생성
    public ProcessInfoEntity toEntity(){
        return ProcessInfoEntity.builder()
                .pcno(this.pcno)
                .pcuuid(this.pcuuid)
                .pcname(this.pcname)
                .pcdesc(this.pcdesc)
                .pcsource(this.pcsource)
//                .pcfilename(this.pcfilename) // OngTK 비활성화
                .build();
    } // func end
} // class end