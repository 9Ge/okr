package com.pm.okr.model.repository;

import com.pm.okr.model.entity.KeyResultEntity;
import com.pm.okr.model.entity.ObjectiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyResultRepository extends JpaRepository<KeyResultEntity,Integer> {

    List<KeyResultEntity> findAllByIdIn(List<Integer> ids);

    KeyResultEntity findByIdAndMu(Integer id, Integer currentMu);
}
