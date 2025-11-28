package lceye.model.entity;

import jakarta.persistence.*;
import lceye.model.dto.UnitGroupDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "unitgroup")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitGroupEntity extends BaseTime{
    // 1. 테이블 설계
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int unsigned")
    private int ugno;
    @Column(columnDefinition = "varchar(30) not null unique")
    private String ugname;
    @Column(columnDefinition = "char(36)")
    private String uguuid;

    // 2. toDto 생성
    public UnitGroupDto toDto(){
        return UnitGroupDto.builder()
                .ugno(this.ugno)
                .ugname(this.ugname)
                .uguuid(this.uguuid)
                .createdate(this.getCreatedate().toString())
                .updatedate(this.getUpdatedate().toString())
                .build();
    } // func end
} // class end