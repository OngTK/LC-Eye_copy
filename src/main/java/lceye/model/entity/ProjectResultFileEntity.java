package lceye.model.entity;

import jakarta.persistence.*;
import lceye.model.dto.ProjectResultFileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_resultfile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResultFileEntity extends BaseTime{
    // 1. 테이블 설계
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int unsigned")
    private int prfno;
    @Column(columnDefinition = "char(40)")
    private String prfname;
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "pjno", columnDefinition = "int unsigned")
    private ProjectEntity projectEntity;

    // 2. toDto 생성
    public ProjectResultFileDto toDto(){
        return ProjectResultFileDto.builder()
                .prfno(this.prfno)
                .prfname(this.prfname)
                .createdate(this.getCreatedate().toString())
                .updatedate(this.getUpdatedate().toString())
                .build();
    } // func end
} // class end