package lceye.model.repository;

import lceye.model.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Integer> {

    @Query(value = "select * from project where mno = :mno",nativeQuery = true)
    List<ProjectEntity> findByMno(int mno);
} // interface end
