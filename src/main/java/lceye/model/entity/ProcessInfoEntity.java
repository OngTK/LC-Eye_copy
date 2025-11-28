package lceye.model.entity;

import jakarta.persistence.*;
import lceye.model.dto.ProcessInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "process_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInfoEntity extends BaseTime{
    // 1. 테이블 설계
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int unsigned")
    private int pcno;
    @Column(columnDefinition = "char(36)")
    private String pcuuid;
    @Column(columnDefinition = "varchar(255) not null")
    private String pcname;
    @Column(columnDefinition = "longtext")
    private String pcdesc;
    @Column(columnDefinition = "varchar(100)")
    private String pcsource;
//    @Column(columnDefinition = "char(41)") // OngTK 비활성화
//    private String pcfilename;

    // 2. toDto 생성
    public ProcessInfoDto toDto(){
        return ProcessInfoDto.builder()
                .pcno(this.pcno)
                .pcuuid(this.pcuuid)
                .pcname(this.pcname)
                .pcdesc(this.pcdesc)
                .pcsource(this.pcsource)
//                .pcfilename(this.pcfilename) // OngTK 비활성화
                .createdate(this.getCreatedate().toString())
                .updatedate(this.getUpdatedate().toString())
                .build();
    } // func end
} // class end