package com.pm.okr.services.objective;

import com.pm.okr.common.ObjectiveMapper;
import com.pm.okr.controller.vo.Objective;
import com.pm.okr.controller.vo.ObjectiveFilter;
import com.pm.okr.controller.vo.ObjectiveNode;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.common.Pair;

import java.util.List;


public interface ObjectiveService {

    List<ObjectiveEntity> addObjective(Integer container, Integer id, Integer year, Integer season, Boolean everyone, String content, String color);

    void updateContent(Integer id, String content, String color);

    List<ObjectiveEntity> updateWeight(Integer id, Double weight);

    List<Objective> getObjectives(List<Integer> ids);

    ObjectiveNode getObjectiveNode(Integer container, Integer id, Boolean withChildren, Integer year, Integer season, ObjectiveFilter filter);

    List<ObjectiveEntity> removeObjective(Integer id);

    ObjectiveMapper.EntityLoader getEntityLoader();

    Pair<ObjectiveEntity, ObjectiveEntity> copyObjectiveTo(Integer oId, Integer container, Integer cid);

    Pair<ObjectiveEntity, ObjectiveEntity> moveObjectiveTo(Integer oId, Integer container, Integer cid);

    void achive(Integer oId);

    void unachive(Integer oId);
}
