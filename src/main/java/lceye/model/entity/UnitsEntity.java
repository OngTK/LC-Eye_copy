package lceye.model.entity;

import jakarta.persistence.*;
import lceye.model.dto.UnitsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "units")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitsEntity extends BaseTime{
    // 1. 테이블 설계
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int unsigned")
    private int uno;
    @Column(columnDefinition = "varchar(30) not null")
    private String unit;
    @Column(columnDefinition = "double not null")
    private double uvalue;
    @Column(columnDefinition = "char(36)")
    private String uuuid;
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "ugno", columnDefinition = "int unsigned not null")
    private UnitGroupEntity unitGroupEntity;

    // 2. toDto 생성
    public UnitsDto toDto(){
        return UnitsDto.builder()
                .uno(this.uno)
                .unit(this.unit)
                .uvalue(this.uvalue)
                .uuuid(this.uuuid)
                .ugno(this.unitGroupEntity.getUgno())
                .ugname(this.unitGroupEntity.getUgname())
                .createdate(this.getCreatedate().toString())
                .updatedate(this.getUpdatedate().toString())
                .build();
    } // func end
} // class end