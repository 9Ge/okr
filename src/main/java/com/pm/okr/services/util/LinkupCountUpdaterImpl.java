package com.pm.okr.services.util;


import com.pm.okr.model.entity.ObjectiveEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class LinkupCountUpdaterImpl implements LinkupCountUpdater {

    OnLinkupCountChangedListener listener;

    @Override
    public void setListener(OnLinkupCountChangedListener listener) {
        this.listener = listener;
    }


    @Override
    public void updateLinkupCount(List<ObjectiveEntity> objectiveEntities) {
        Set<String> entitiesSet = new HashSet<>();
        for (ObjectiveEntity objectiveEntity : objectiveEntities) {
            String key = objectiveEntity.getContainerType() + "-" +
                    objectiveEntity.getContainerId() + "-" +
                    objectiveEntity.getYear() + "-" +
                    objectiveEntity.getSeason();
            if (!entitiesSet.contains(key)) {
                updateLinkupCount(objectiveEntity);
                entitiesSet.add(key);
            }
        }
    }


    @Override
    public void updateLinkupCount(ObjectiveEntity objectiveEntity) {
        updateLinkupCount(
                objectiveEntity.getContainerType(),
                objectiveEntity.getContainerId(),
                objectiveEntity.getYear(),
                objectiveEntity.getSeason()
        );
    }


    @Override
    public void updateLinkupCount(Integer containerType, Integer containerId, Integer year, Integer season) {
        if (null != listener) {
            listener.onChanged(containerType, containerId, year, season);
        }
    }


}
