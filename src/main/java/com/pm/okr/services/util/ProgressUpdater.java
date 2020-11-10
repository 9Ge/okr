package com.pm.okr.services.util;

import com.pm.okr.model.entity.ObjectiveEntity;

import java.util.List;


public interface ProgressUpdater {

    public interface ProgressListener{
        void onProgressChanged(List<ObjectiveEntity> objs);
    }


    void setListener(ProgressUpdaterImpl.ProgressListener listener);

    List<ObjectiveEntity> updateObjective(Integer objId);


    void notifyProgressChanged(List<ObjectiveEntity> oes);

}
