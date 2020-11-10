package com.pm.okr.services.link;

import com.pm.okr.common.KeyResultMapper;
import com.pm.okr.common.ObjectiveMapper;
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
public class LinkServiceImpl implements LinkService {

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
    ObjectiveMapper.EntityLoader entityLoader;

    @Autowired
    ProgressUpdater progressUpdater;

    @Autowired
    LinkupCountUpdater linkupCountUpdater;


    @Override
    public List<Objective.PartToLink> getLinkObjectives(Integer id) {
        ObjectiveEntity oe = objectiveRepository.findOneById(id);
        if (oe != null) {
            List<ObjectiveEntity> oes = objectiveRepository.findValidLinkObjectives(
                    id,
                    oe.getContainerType(),
                    oe.getYear(),
                    oe.getSeason(),
                    SessionUtil.currentMu());

            //去除环状链接
            for (int i = oes.size() - 1; i >= 0; --i){
                if (hasLinkCycle(id, oes.get(i).getId())){
                    oes.remove(i);
                }
            }

            return ObjectiveMapper.toPartToLinks(oes, entityLoader);
        }
        return Collections.emptyList();
    }


    @Override
    public boolean hasLinkCycle(Integer start, Integer end) {
        if (start.equals(end)){
            return true;
        }
        Set<Integer> oeIds = new HashSet<>();
        oeIds.add(start);
        ObjectiveEntity oe = objectiveRepository.findOneById(end);
        while (oe != null && oe.getLinkAbove() != null){
            if (oeIds.contains(oe.getLinkAbove())){
                return true;
            }else{
                oeIds.add(oe.getLinkAbove());
                oe = objectiveRepository.findOneById(oe.getLinkAbove());
            }
        }
        return false;
    }

    @Override
    public List<ObjectiveEntity> linkObjective(Integer start, Integer end) {
        ObjectiveEntity oeStart = objectiveRepository.findOneById(start);
        ObjectiveEntity oeEnd = objectiveRepository.findOneById(end);
        if (null != oeStart &&
                oeEnd != null &&
                oeStart.getContainerType() >= oeEnd.getContainerType() &&
                !hasLinkCycle(start, end)) {
            Integer oldLinkAbove = oeStart.getLinkAbove();
            if (oldLinkAbove != end) {
                oeStart.setLinkAbove(end);
                oeStart.setLinkTime(new Timestamp(System.currentTimeMillis()));
                objectiveRepository.saveAndFlush(oeStart);
                List<ObjectiveEntity> oes = progressUpdater.updateObjective(end);
                if (oldLinkAbove != null) {
                    oes.addAll(progressUpdater.updateObjective(oldLinkAbove));
                }
                linkupCountUpdater.updateLinkupCount(oeStart);
                return oes;
            }
            return Collections.emptyList();
        }
        return null;
    }

    @Override
    public List<ObjectiveEntity> breakLinkAbove(Integer id) {
        ObjectiveEntity oe = objectiveRepository.findOneById(id);
        if (oe != null) {
            Integer linkAbove = oe.getLinkAbove();
            if (linkAbove == null) {
                return Collections.emptyList();
            } else {
                oe.setLinkAbove(null);
                objectiveRepository.saveAndFlush(oe);
                linkupCountUpdater.updateLinkupCount(oe);
                return progressUpdater.updateObjective(linkAbove);
            }
        }
        return null;
    }

    @Override
    public ResultKRChanged breakLinkBelow(Integer id) {
        ObjectiveEntity oe = objectiveRepository.findOneById(id);
        if (oe != null) {
            Integer linkAbove = oe.getLinkAbove();
            if (linkAbove == null) {
                return new ResultKRChanged();
            } else if (oe.getAssignFrom() != null) {
                KeyResultEntity ke = new KeyResultEntity();
                ke.setWeight(oe.getWeight());
                ke.setContent(oe.getContent());
                ke.setObjId(linkAbove);
                ke.setCreateTime(new Timestamp(System.currentTimeMillis()));
                ke.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                ke.setMu(SessionUtil.currentMu());
                ke = keyResultRepository.saveAndFlush(ke);
                objectiveRepository.deleteById(id);
                objectiveRepository.flush();
                linkupCountUpdater.updateLinkupCount(oe);
                ResultKRChanged rkc = new ResultKRChanged();
                rkc.setKR(KeyResultMapper.toPartShort(ke));
                rkc.setObjectiveProgress(ProgressUtil.computeProgresses(progressUpdater.updateObjective(linkAbove), false));
                return rkc;
            } else {
                oe.setLinkAbove(null);
                ResultKRChanged rkc = new ResultKRChanged();
                rkc.setObjectiveProgress(ProgressUtil.computeProgresses(progressUpdater.updateObjective(linkAbove), false));
                objectiveRepository.saveAndFlush(oe);
                linkupCountUpdater.updateLinkupCount(oe);
                return rkc;
            }
        }
        return null;
    }
}
