package lceye.model.entity;

import jakarta.persistence.*;
import lceye.model.dto.ProjectDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity extends BaseTime{
    // 1. 테이블 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int unsigned")
    private int pjno;           // 프로젝트 번호
    @Column(columnDefinition = "varchar(100)")
    private String pjname;      // 프로젝트 명
    @Column(columnDefinition = "double not null")
    private double pjamount;    // 생산량
    @Column(columnDefinition = "longtext")
    private String pjdesc;      // 프로젝트 설명
    @Column(columnDefinition = "char(40)")
    private String pjfilename;  // 프로젝트 정보 저장 파일
    @Column(columnDefinition = "int unsigned not null")
    private int mno;            // 작성자
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "uno", columnDefinition = "int unsigned")
    private UnitsEntity unitsEntity;    // 상세 단위 번호

    // 2. toDto 생성
    public ProjectDto toDto(){
        return ProjectDto.builder()
                .pjno(this.pjno)
                .pjname(this.pjname)
                .pjamount(this.pjamount)
                .pjdesc(this.pjdesc)
                .pjfilename(this.pjfilename)
                .mno(this.mno)
                .uno(this.unitsEntity.getUno())
                .createdate(this.getCreatedate().toString())
                .updatedate(this.getUpdatedate().toString())
                // 부가정보
                .unit(this.unitsEntity.getUnit())
                .ugno(this.unitsEntity.getUnitGroupEntity().getUgno())
                .ugname(this.unitsEntity.getUnitGroupEntity().getUgname())
                .build();
    } // func end
} // class end