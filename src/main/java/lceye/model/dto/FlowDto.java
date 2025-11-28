package lceye.model.dto;

import lceye.model.entity.FlowEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowDto {
    // 1. 기본적인 정보
    private int fno;
    private String fname;
    private String fuuid;
    private String casnumber;
    private int uno;
    private String createdate;
    private String updatedate;

    // 2. 부가적인 정보

    // 3. toEntity 생성
    public FlowEntity toEntity(){
        return FlowEntity.builder()
                .fno(this.fno)
                .fname(this.fname)
                .fuuid(this.fuuid)
                .casnumber(this.casnumber)
                .build();
    } // func end
} // class end