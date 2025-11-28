package lceye.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import lceye.model.entity.CompanyEntity;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Integer> {
} // interface end