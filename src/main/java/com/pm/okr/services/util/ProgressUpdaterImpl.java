package com.pm.okr.services.util;

import com.pm.okr.model.entity.KeyResultEntity;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.model.repository.ObjectiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class ProgressUpdaterImpl implements ProgressUpdater {

    @Autowired
    ObjectiveRepository objectiveRepository;

    ProgressListener listener;

    @Override
    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

    void updateWeight(ObjectiveEntity obj) {
        List<KeyResultEntity> krs = obj.getKeyResults();
        List<ObjectiveEntity> objs = obj.getLinkBelows();
        Double weightSum = 0d;
        Double progressSum = 0d;
        for (KeyResultEntity kr : krs) {
            progressSum += kr.getWeight() * kr.getProgress();
            weightSum += kr.getWeight();
        }
        for (ObjectiveEntity o : objs) {
            progressSum += o.getWeight() * o.getProgress();
            weightSum += o.getWeight();
        }

        Double progress = (weightSum == 0) ? 0d: progressSum / weightSum;
        obj.setProgress(progress);
        objectiveRepository.saveAndFlush(obj);
    }

    List<ObjectiveEntity> refreshObjective(Integer objId) {
        Optional<ObjectiveEntity> optObj = objectiveRepository.findById(objId);
        if (optObj.isPresent()) {
            List<ObjectiveEntity> objEffects = new ArrayList<>();
            ObjectiveEntity obj = optObj.get();
            objEffects.add(obj);
            updateWeight(obj);
            if (obj.getLinkAbove() != null) {
                objEffects.addAll(refreshObjective(obj.getLinkAbove()));
            }
            return objEffects;
        }
        return Collections.emptyList();
    }

    @Override
    public List<ObjectiveEntity> updateObjective(Integer objId) {
        List<ObjectiveEntity> ret = refreshObjective(objId);
        notifyProgressChanged(ret);
        return ret;
    }

    @Override
    public void notifyProgressChanged(List<ObjectiveEntity> oes){
        if (null != listener && !oes.isEmpty()){
            listener.onProgressChanged(oes);
        }
    }
}
