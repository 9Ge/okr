package com.pm.okr.model.repository;

import com.pm.okr.model.entity.CompanyEntity;
import com.pm.okr.model.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompanyRepository extends JpaRepository<CompanyEntity,Integer> {

    List<CompanyEntity> findAllByMu(Integer currentMu);
}
