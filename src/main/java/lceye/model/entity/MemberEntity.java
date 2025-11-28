package lceye.model.entity;

import jakarta.persistence.*;
import lceye.model.dto.MemberDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity extends BaseTime{
    // 1. 테이블 설계
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int unsigned")
    private int mno;
    @Column(columnDefinition = "varchar(30) not null")
    private String mname;
    @Column(columnDefinition = "varchar(18) not null unique")
    private String mid;
    @Column(columnDefinition = "varchar(18) not null")
    private String mpwd;
    @Column(columnDefinition = "enum('ADMIN', 'MANAGER', 'WORKER') default 'WORKER'")
    private String mrole;
    @Column(columnDefinition = "varchar(50) not null unique")
    private String memail;
    @Column(columnDefinition = "varchar(13) not null unique")
    private String mphone;
    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "cno", columnDefinition = "int unsigned not null")
    private CompanyEntity companyEntity;

    // 2. toDto 생성
    public MemberDto toDto(){
        return MemberDto.builder()
                .mno(this.mno)
                .mname(this.mname)
                .mid(this.mid)
                .mrole(this.mrole)
                .memail(this.memail)
                .mphone(this.mphone)
                .mpwd(this.mpwd)
                .mphone(this.mphone)
                .createdate(this.getCreatedate().toString())
                .updatedate(this.getUpdatedate().toString())
                .build();
    } // func end
} // class end