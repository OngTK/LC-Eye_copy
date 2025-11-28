package lceye.model.entity;

import jakarta.persistence.*;
import lceye.model.dto.CompanyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEntity extends BaseTime{
    // 1. 테이블 설계
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "int unsigned")
    private int cno;
    @Column(columnDefinition = "varchar(50) not null unique")
    private String cname;
    @Column(columnDefinition = "varchar(255) not null unique")
    private String caddress;
    @Column(columnDefinition = "varchar(13) unique")
    private String ctel;
    @Column(columnDefinition = "varchar(30) not null")
    private String cowner;

    // 2. toDto 생성
    public CompanyDto toDto(){
        return CompanyDto.builder()
                .cno(this.cno)
                .cname(this.cname)
                .ctel(this.ctel)
                .cowner(this.cowner)
                .createdate(this.getCreatedate().toString())
                .updatedate(this.getUpdatedate().toString())
                .build();
    } // func end
} // class end