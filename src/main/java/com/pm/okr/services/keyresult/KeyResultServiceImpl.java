package com.pm.okr.services.keyresult;

import com.pm.okr.common.KeyResultMapper;
import com.pm.okr.common.ProgressUtil;
import com.pm.okr.common.SessionUtil;
import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.*;
import com.pm.okr.model.repository.*;
import com.pm.okr.services.util.LinkupCountUpdater;
import com.pm.okr.services.util.ProgressUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class KeyResultServiceImpl implements KeyResultService {


    @Autowired
    UserRepository userRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    ObjectiveRepository objectiveRepository;

    @Autowired
    KeyResultRepository keyResultRepository;

    @Autowired
    ProgressUpdater progressUpdater;

    @Autowired
    LinkupCountUpdater linkupCountUpdater;

    @Override
    public ResultKRChanged addKeyResult(Integer oId, String content, String resultType, Double startValue, Double targetValue, Integer decimals) {
        ObjectiveEntity oe = objectiveRepository.findOneById(oId);
        if (null != oe) {
            KeyResultEntity kr = new KeyResultEntity();
            kr.setCreateTime(new Timestamp(System.currentTimeMillis()));
            kr.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            kr.setObjId(oId);
            kr.setContent(content);
            if (null != resultType) {
                kr.setResultType(resultType);
            }
            if (null != startValue) {
                kr.setStartValue(startValue);
            }
            if (null != targetValue) {
                kr.setTargetValue(targetValue);
            }
            if (null != targetValue) {
                kr.setTargetValue(targetValue);
            }
            if (null != decimals) {
                kr.setDecimals(decimals);
            }
            kr.setMu(SessionUtil.currentMu());
            kr = keyResultRepository.saveAndFlush(kr);
            List<ObjectiveEntity> progresses = progressUpdater.updateObjective(oId);
            ResultKRChanged resultKRChanged = new ResultKRChanged();
            resultKRChanged.setKR(KeyResultMapper.toPartShort(kr));
            resultKRChanged.setObjectiveProgress(ProgressUtil.computeProgresses(progresses, false));
            return resultKRChanged;
        }
        return null;
    }

    @Override
    public boolean updateContent(Integer id, String content) {
        Optional<KeyResultEntity> ret = keyResultRepository.findById(id);
        if (ret.isPresent()) {
            KeyResultEntity kre = ret.get();
            kre.setContent(content);
            keyResultRepository.saveAndFlush(kre);
            return true;
        }
        return false;
    }

    @Override
    public KeyResultEntity updateTypeValue(Integer id, String resultType, Double startValue, Double targetValue, Integer decimals) {
        Optional<KeyResultEntity> ret = keyResultRepository.findById(id);
        if (ret.isPresent()) {
            KeyResultEntity kre = ret.get();
            if (resultType != null) {
                kre.setResultType(resultType);
            }
            if (startValue != null) {
                kre.setStartValue(startValue);
            }
            if (targetValue != null) {
                kre.setTargetValue(targetValue);
            }
            if (decimals != null) {
                kre.setDecimals(decimals);
            }
            keyResultRepository.saveAndFlush(kre);
            return kre;
        }
        return null;
    }

    @Override
    public List<ObjProgress> updateWeight(Integer id, Double weight) {
        Optional<KeyResultEntity> ret = keyResultRepository.findById(id);
        if (ret.isPresent()) {
            KeyResultEntity kre = ret.get();
            kre.setWeight(weight);
            keyResultRepository.saveAndFlush(kre);
            List<ObjectiveEntity> progresses = progressUpdater.updateObjective(kre.getObjId());
            return ProgressUtil.computeProgresses(progresses, false);
        }
        return null;
    }

    @Override
    public List<ObjProgress> updateProgress(Integer id, Double progress) {
        Optional<KeyResultEntity> ret = keyResultRepository.findById(id);
        if (ret.isPresent()) {
            KeyResultEntity kre = ret.get();
            kre.setProgress(progress);
            kre.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            keyResultRepository.saveAndFlush(kre);
            List<ObjectiveEntity> progresses = progressUpdater.updateObjective(kre.getObjId());
            return ProgressUtil.computeProgresses(progresses, false);
        }
        return null;
    }

    @Override
    public List<KeyResultEntity> findAll(List<Integer> ids) {
        return keyResultRepository.findAllByIdIn(ids);
    }

    @Override
    public List<ObjProgress> removeKeyResult(Integer id) {
        KeyResultEntity kre = keyResultRepository.findByIdAndMu(id, SessionUtil.currentMu());
        if (kre != null) {
            keyResultRepository.deleteById(id);
            keyResultRepository.flush();
            List<ObjectiveEntity> progresses = progressUpdater.updateObjective(kre.getObjId());
            return ProgressUtil.computeProgresses(progresses, false);
        }
        return null;
    }

    @Override
    public List<ObjProgress> assignResult(Integer kId, Integer container, Integer cId) {
        Optional<KeyResultEntity> ret = keyResultRepository.findById(kId);
        if (ret.isPresent()) {

            KeyResultEntity kre = ret.get();
            ObjectiveEntity pOe = objectiveRepository.findOneById(kre.getObjId());
            if (null == pOe){
                return null;
            }

            switch (container) {
                case ObjectiveContainer.COMPANY:
                    if (!companyRepository.findById(cId).isPresent()){
                        return null;
                    }
                    break;
                case ObjectiveContainer.TEAM:
                    if (!teamRepository.findById(cId).isPresent()){
                        return null;
                    }
                    break;
                case ObjectiveContainer.USER:
                    if (!userRepository.findById(cId).isPresent()){
                        return null;
                    }
                    break;
                default:
                    return null;
            }


            ObjectiveEntity oe = new ObjectiveEntity();
            oe.setYear(pOe.getYear());
            oe.setSeason(pOe.getSeason());
            oe.setContent(kre.getContent());
            oe.setWeight(kre.getWeight());
            oe.setColor(pOe.getColor());
            oe.setContainerType(container);
            oe.setContainerId(cId);
            oe.setCreateTime(new Timestamp(System.currentTimeMillis()));
            oe.setLinkTime(new Timestamp(System.currentTimeMillis()));
            oe.setLinkAbove(pOe.getId());
            oe.setAssignFrom(pOe.getId());
            oe.setMu(kre.getMu());
            keyResultRepository.deleteById(kre.getId());
            keyResultRepository.flush();

            oe = objectiveRepository.saveAndFlush(oe);

            linkupCountUpdater.updateLinkupCount(oe);

            List<ObjectiveEntity> progresses = new ArrayList<>();
            progresses.add(oe);

            progresses.addAll(progressUpdater.updateObjective(kre.getObjId()));

            return ProgressUtil.computeProgresses(progresses, false);
        }
        return null;
    }
}
