package lceye.model.dto;

import lceye.model.entity.UnitsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitsDto {
    // 1. 기본적인 정보
    private int uno;
    private String unit;
    private int ugno;
    private double uvalue;
    private String uuuid;
    private String createdate;
    private String updatedate;

    // 2. 부가적인 정보
    private String ugname;


    // 3. toEntity 생성
    public UnitsEntity toEntity(){
        return UnitsEntity.builder()
                .uno(this.uno)
                .unit(this.unit)
                .uvalue(this.uvalue)
                .uuuid(this.uuuid)
                .build();
    } // func end
} // class end