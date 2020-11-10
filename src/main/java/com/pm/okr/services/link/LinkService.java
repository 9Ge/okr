package com.pm.okr.services.link;

import com.pm.okr.controller.vo.Objective;
import com.pm.okr.controller.vo.ResultKRChanged;
import com.pm.okr.model.entity.ObjectiveEntity;

import java.util.List;


public interface LinkService {


    List<Objective.PartToLink> getLinkObjectives(Integer id);

    boolean hasLinkCycle(Integer start, Integer end);

    List<ObjectiveEntity> linkObjective(Integer start, Integer end);

    List<ObjectiveEntity> breakLinkAbove(Integer valueOf);

    ResultKRChanged breakLinkBelow(Integer valueOf);
}
