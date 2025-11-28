package lceye.model.dto;

import lceye.model.entity.CompanyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    // 1. 기본적인 정보
    private int cno;
    private String cname;
    private String ctel;
    private String cowner;
    private String createdate;
    private String updatedate;

    // 2. 부가적인 정보


    // 3. toEntity 생성
    public CompanyEntity toEntity(){
        return CompanyEntity.builder()
                .cno(this.cno)
                .cname(this.cname)
                .ctel(this.ctel)
                .cowner(this.cowner)
                .build();
    } // func end
} // class end