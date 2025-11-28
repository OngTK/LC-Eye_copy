package lceye.model.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // Auditing 활성화
public class BaseTime {
    @CreatedDate                        // 현재 날짜 및 시간을 자동으로 주입
    private LocalDateTime createdate;   // 생성 날짜 및 시간
    @LastModifiedDate                   // 수정 날짜 및 시간을 자동으로 주입
    private LocalDateTime updatedate;   // 수정 날짜 및 시간
} // class end