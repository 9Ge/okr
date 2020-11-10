package com.pm.okr.services.util;


import com.pm.okr.model.entity.ObjectiveEntity;

import java.util.List;

public interface LinkupCountUpdater {
    interface OnLinkupCountChangedListener {
        void onChanged(Integer containerType, Integer containerId, Integer year, Integer season);
    }


    void setListener(OnLinkupCountChangedListener listener);

    void updateLinkupCount(List<ObjectiveEntity> objectiveEntities);

    void updateLinkupCount(ObjectiveEntity objectiveEntity);

    void updateLinkupCount(Integer containerType, Integer containerId, Integer year, Integer season);

}
