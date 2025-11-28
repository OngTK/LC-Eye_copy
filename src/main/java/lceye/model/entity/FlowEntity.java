package lceye.model.entity;

import jakarta.persistence.*;
import lceye.model.dto.FlowDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flow")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowEntity extends BaseTime{
    // 1. 테이블 설계
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int unsigned")
    private int fno;
    @Column(columnDefinition = "varchar(255) not null")
    private String fname;
    @Column(columnDefinition = "char(36)")
    private String fuuid;
    @Column(columnDefinition = "varchar(120)")
    private String casnumber;
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "uno", columnDefinition = "int unsigned")
    private UnitsEntity unitsEntity;

    // 2. toDto 생성
    public FlowDto toDto(){
        return FlowDto.builder()
                .fno(this.fno)
                .fname(this.fname)
                .fuuid(this.fuuid)
                .casnumber(this.casnumber)
                .createdate(this.getCreatedate().toString())
                .updatedate(this.getUpdatedate().toString())
                .build();
    } // func end
} // class end