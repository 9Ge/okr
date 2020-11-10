package com.pm.okr.services.objective;

import com.pm.okr.common.*;
import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.*;
import com.pm.okr.model.repository.*;
import com.pm.okr.services.util.LinkupCountUpdater;
import com.pm.okr.services.util.ProgressUpdater;
import com.pm.okr.common.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class ObjectiveServiceImpl implements ObjectiveService {

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
    public List<ObjectiveEntity> addObjective(Integer container, Integer id, Integer year, Integer season, Boolean everyone, String content, String color) {
        ObjectiveEntity oe = new ObjectiveEntity();
        oe.setContainerType(container);
        oe.setContainerId(id);
        oe.setYear(year);
        oe.setSeason(season);
        oe.setContent(content);
        oe.setColor(color);
        oe.setMu(SessionUtil.currentMu());
        oe.setCreateTime(new Timestamp(System.currentTimeMillis()));

        if (ObjectiveContainer.COMPANY == container) {
            if (!companyRepository.findById(id).isPresent()) {
                return null;
            }
        } else if (ObjectiveContainer.TEAM == container) {
            if (!teamRepository.findById(id).isPresent()) {
                return null;
            }

        } else if (ObjectiveContainer.USER == container) {
            if (!userRepository.findById(id).isPresent()) {
                return null;
            }
        }
        List<ObjectiveEntity> oes = new ArrayList<>();
        if (everyone) {
            List<UserEntity> us = null;
            if (ObjectiveContainer.COMPANY == container) {
                us = userRepository.findAllByMu(SessionUtil.currentMu());
            } else if (ObjectiveContainer.TEAM == container) {
                us = userRepository.findTeamUser(id);
            } else if (ObjectiveContainer.USER == container) {
                us = Arrays.asList(userRepository.findById(id).get());
            }
            for (UserEntity u : us) {
                ObjectiveEntity oe2 = BeanUtil.fill(oe, new ObjectiveEntity());
                oe2.setContainerType(ObjectiveContainer.USER);
                oe2.setContainerId(u.getId());
                oe2.setMu(SessionUtil.currentMu());
                oes.add(objectiveRepository.saveAndFlush(oe2));
            }
        } else {
            oes.add(objectiveRepository.saveAndFlush(oe));
        }

        progressUpdater.notifyProgressChanged(oes);

        linkupCountUpdater.updateLinkupCount(oes);

        return oes;
    }

    @Override
    public void updateContent(Integer id, String content, String color) {
        Optional<ObjectiveEntity> oe = objectiveRepository.findById(id);
        if (oe.isPresent()) {
            oe.get().setContent(content);
            if (color != null) {
                oe.get().setColor(color);
            }
            objectiveRepository.saveAndFlush(oe.get());
        }
    }

    @Override
    public List<ObjectiveEntity> updateWeight(Integer id, Double weight) {
        Optional<ObjectiveEntity> oe = objectiveRepository.findById(id);
        if (oe.isPresent()) {
            oe.get().setWeight(weight);
            objectiveRepository.saveAndFlush(oe.get());

            List<ObjectiveEntity> oes = new ArrayList<>();
            oes.add(oe.get());
            oes.addAll(progressUpdater.updateObjective(oe.get().getLinkAbove()));
            return oes;
        }
        return Collections.emptyList();
    }

    @Override
    public List<Objective> getObjectives(List<Integer> ids) {
        return ObjectiveMapper.fromList(objectiveRepository.findAllByIdInAndMu(ids, SessionUtil.currentMu()), entityLoader, null, null);
    }

    class WeekCountFilter implements KeyResultMapper.BeforeMapping {

        ObjectiveFilter filter;

        public WeekCountFilter(ObjectiveFilter filter) {
            this.filter = filter;
        }

        @Override
        public boolean filter(KeyResultEntity kr) {
            if (filter != null) {
                int weekCount = ProgressUtil.numOfWeek(kr.getCreateTime().getTime(), System.currentTimeMillis());
                switch (filter.getKrs()) {
                    case ObjectiveFilter.KR.WEEK2:
                        return weekCount <= 2;
                    case ObjectiveFilter.KR.WEEK4:
                        return weekCount <= 4;
                    case ObjectiveFilter.KR.WEEK8:
                        return weekCount <= 8;
                }
            }
            return true;
        }
    }

    class RangeAndStatusFilter implements ObjectiveMapper.BeforeMapping {
        ObjectiveFilter filter;

        public RangeAndStatusFilter(ObjectiveFilter filter) {
            this.filter = filter;
        }

        @Override
        public boolean filter(ObjectiveEntity oe) {
            if (filter != null) {

                switch (filter.getRange()) {
                    case ObjectiveFilter.Progress.P33:
                        return oe.getProgress() < 34;
                    case ObjectiveFilter.Progress.P66:
                        return (oe.getProgress() >= 34 && oe.getProgress() < 67);
                    case ObjectiveFilter.Progress.P100:
                        return oe.getProgress() >= 67;
                }

                if (filter.getStatus() > ObjectiveFilter.Status.ALL &&
                        filter.getStatus() <= ObjectiveFilter.Status.EXCEEDED) {
                    ObjProgress progress = ProgressUtil.computeProgress(oe, null, false);
                    return progress.getStatus() == (filter.getStatus() - 1);
                }
            }
            return true;
        }
    }

    @Override
    public ObjectiveNode getObjectiveNode(Integer container, Integer id, Boolean withChildren, Integer year, Integer season, ObjectiveFilter filter) {
        ObjectiveMapper.BeforeMapping objFilter = new RangeAndStatusFilter(filter);
        KeyResultMapper.BeforeMapping krFilter = new WeekCountFilter(filter);
        switch (container) {
            case ObjectiveContainer.COMPANY:
                return buildCompanyNode(container, id, withChildren, year, season, objFilter, krFilter);
            case ObjectiveContainer.TEAM:
                return buildTeamNode(container, id, withChildren, year, season, objFilter, krFilter);
            case ObjectiveContainer.USER:
                return buildUserNode(container, id, year, season, objFilter, krFilter);
        }
        return null;
    }

    @Override
    public List<ObjectiveEntity> removeObjective(Integer id) {
        ObjectiveEntity obj = objectiveRepository.findByIdAndMu(id, SessionUtil.currentMu());
        if (null != obj) {

            for (ObjectiveEntity oe : obj.getLinkBelows()) {
                oe.setLinkAbove(null);
            }
            objectiveRepository.saveAll(obj.getLinkBelows());

            objectiveRepository.delete(obj);

            objectiveRepository.flush();


            progressUpdater.notifyProgressChanged(Arrays.asList(obj));

            List<ObjectiveEntity> oes = new ArrayList<>();

            oes.addAll(obj.getLinkBelows());
            oes.add(obj);

            linkupCountUpdater.updateLinkupCount(obj);


            if (obj.getLinkAbove() != null) {
                ObjectiveEntity objAbove = objectiveRepository.findByIdAndMu(obj.getLinkAbove(), SessionUtil.currentMu());
                if (null != objAbove) {
//                    KeyResultEntity kr = new KeyResultEntity();
//                    kr.setContent(obj.getContent());
//                    kr.setCreateTime(obj.getCreateTime());
//                    kr.setProgress(0d);...Object.assign(
//                    kr.setObjId(objAbove.getId());
//                    kr.setWeight(obj.getWeight());
//                    kr.setUpdateTime(new Timestamp(System.currentTimeMillis()));
//                    kr.setCreateTime(new Timestamp(System.currentTimeMillis()));
//                    keyResultRepository.saveAndFlush(kr);
                    return progressUpdater.updateObjective(objAbove.getId());
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public ObjectiveMapper.EntityLoader getEntityLoader() {
        return entityLoader;
    }

    @Override
    public Pair<ObjectiveEntity, ObjectiveEntity> copyObjectiveTo(Integer oId, Integer container, Integer cid) {
        ObjectiveEntity oFrom = objectiveRepository.findByIdAndMu(oId, SessionUtil.currentMu());
        Objective.Owner owner = ObjectiveMapper.getOwner(container, cid, entityLoader);
        if (oFrom != null && owner != null) {
            ObjectiveEntity oe = new ObjectiveEntity();
            BeanUtil.fill(oFrom, oe);
            oe.setId(null);
            oe.setLinkAbove(null);
            oe.setLinkBelows(new ArrayList<>());
            oe.setLinkTime(null);
            oe.setCreateTime(new Timestamp(System.currentTimeMillis()));
            oe.setAssignFrom(null);
            oe.setContainerType(container);
            oe.setContainerId(cid);
            oe.setKeyResults(new ArrayList<>(oFrom.getKeyResults()));
            oe = objectiveRepository.saveAndFlush(oe);
            linkupCountUpdater.updateLinkupCount(oe);
            return new Pair<>(oFrom, oe);
        }
        return null;
    }

    @Override
    public Pair<ObjectiveEntity, ObjectiveEntity> moveObjectiveTo(Integer oId, Integer container, Integer cid) {
        ObjectiveEntity oFrom = objectiveRepository.findByIdAndMu(oId, SessionUtil.currentMu());
        Objective.Owner owner = ObjectiveMapper.getOwner(container, cid, entityLoader);
        if (oFrom != null && owner != null) {
            if (oFrom.getLinkAbove() != null) {
                return null;
            }
            ObjectiveEntity oFromCopy = BeanUtil.fill(oFrom, new ObjectiveEntity());
            oFrom.setContainerType(container);
            oFrom.setContainerId(cid);
            oFrom = objectiveRepository.saveAndFlush(oFrom);
            progressUpdater.updateObjective(oId);
            linkupCountUpdater.updateLinkupCount(Arrays.asList(oFromCopy, oFrom));
            return new Pair<>(oFromCopy, oFrom);
        }
        return null;
    }

    @Override
    public void achive(Integer oId) {
        ObjectiveEntity oe = objectiveRepository.findByIdAndMu(oId, SessionUtil.currentMu());
        if (null != oe) {
            oe.setArchived(true);
        }
        objectiveRepository.save(oe);
    }

    @Override
    public void unachive(Integer oId) {
        ObjectiveEntity oe = objectiveRepository.findByIdAndMu(oId, SessionUtil.currentMu());
        if (null != oe) {
            oe.setArchived(false);
        }
        objectiveRepository.save(oe);
    }

    ObjectiveNode create(
            Integer container,
            Integer id,
            Integer year,
            Integer season,
            ObjectiveMapper.BeforeMapping oFilter,
            KeyResultMapper.BeforeMapping kFilter) {
        List<ObjectiveEntity> oes = null;
        if (season != null) {
            oes = objectiveRepository.findAllByContainerSeasonAndMu(container, id, year, season, SessionUtil.currentMu());
        } else {
            oes = objectiveRepository.findAllByContainerAndMu(container, id, year, SessionUtil.currentMu());
        }
        ObjectiveNode on = new ObjectiveNode();
        on.setContainer(new Objective.Owner());
        on.setObjectives(ObjectiveMapper.fromList(oes, entityLoader, oFilter, kFilter));
        on.setChildren(new ArrayList<>());
        return on;
    }

    private ObjectiveNode buildTeamNode(Integer container, Integer id, Boolean withChildren, Integer year, Integer season, ObjectiveMapper.BeforeMapping oFilter, KeyResultMapper.BeforeMapping kFilter) {
        TeamEntity team = teamRepository.findById(id).get();
        ObjectiveNode on = create(container, id, year, season, oFilter, kFilter);
        on.getContainer().setTeam(BeanUtil.fill(team, new Team.Part()));
        if (withChildren) {
            List<UserEntity> users = userRepository.findTeamUser(id);
            for (UserEntity user : users) {
                on.getChildren().add(buildUserNode(ObjectiveContainer.USER, user.getId(), year, season, oFilter, kFilter));
            }
        }
        return on;
    }

    private ObjectiveNode buildUserNode(
            Integer container,
            Integer id,
            Integer year,
            Integer season,
            ObjectiveMapper.BeforeMapping oFilter,
            KeyResultMapper.BeforeMapping kFilter) {
        UserEntity user = userRepository.findById(id).get();
        ObjectiveNode on = create(container, id, year, season, oFilter, kFilter);
        on.getContainer().setUser(BeanUtil.fill(user, new User.PartShort()));
        return on;

    }

    private ObjectiveNode buildCompanyNode(
            Integer container,
            Integer id,
            Boolean withChildren,
            Integer year,
            Integer season,
            ObjectiveMapper.BeforeMapping oFilter,
            KeyResultMapper.BeforeMapping kFilter) {
        CompanyEntity ce = companyRepository.findById(id).get();
        ObjectiveNode on = create(container, id, year, season, oFilter, kFilter);
        on.getContainer().setCompany(BeanUtil.fill(ce, new Company.Part()));
        if (withChildren) {
            List<TeamEntity> teams = teamRepository.findAllByMu(SessionUtil.currentMu());
            for (TeamEntity team : teams) {
                on.getChildren().add(buildTeamNode(ObjectiveContainer.TEAM, team.getId(), withChildren, year, season, oFilter, kFilter));
            }
        }
        return on;
    }
}
