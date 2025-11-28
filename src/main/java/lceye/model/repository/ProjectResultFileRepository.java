package lceye.model.repository;

import lceye.model.entity.ProjectResultFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectResultFileRepository extends JpaRepository<ProjectResultFileEntity, Integer> {

    /**
     * pjno를 매개변수로 하여 가장 최근에 생성된 레코드의 파일명을 반환
     */
    @Query(value = "select prfname from project_resultfile where pjno= :pjno order by createdate desc limit 1 ;", nativeQuery = true)
    String returnFilename(int pjno);

    @Query(value = "SELECT prfno FROM project_resultfile WHERE pjno = :pjno", nativeQuery = true)
    int getPrfno(int pjno);

    @Query(value = "update project_resultfile set prfname = '' where prfno = :prfno", nativeQuery = true)
    void nullFileName(int prfno);
} // interface end