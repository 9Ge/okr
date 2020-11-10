package com.pm.okr.services.analysis;

import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.ObjectiveEntity;

import java.util.List;

public interface BIService {

    void collectData(int year, int season, int week);

    LinkedCount getLinkedCount(Integer container, Integer containerId, ParamParser.YearSeason ys);

    List<WeekProgress> getObjectiveWeekProgress(Integer container, Integer containerId, ParamParser.YearSeason ys) throws IllegalAccessException, InstantiationException;

    StatusCount getStatusCount(Integer container, Integer id, ParamParser.YearSeason ys);

    WeekProcessCompositeCategory getCompositeProgress(Integer container, Integer id, ParamParser.YearSeason ys) throws IllegalAccessException, InstantiationException;

    LatestWeekProcessMap getLatestProgress(ParamParser.YearSeason ys);

//    void updateLinkupCount(Integer container, Integer containerId, ParamParser.YearSeason ys);


}
