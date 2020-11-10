package com.pm.okr.services.analysis;

import com.pm.okr.common.BeanUtil;
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
public class BIServiceImpl implements BIService {


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
    ObjectiveLinkCountRepository objectiveLinkCountRepository;

    @Autowired
    ObjectiveWeekProgressRepository objectiveWeekProgressRepository;

    @Autowired
    ProgressUpdater progressUpdater;

    @Autowired
    LinkupCountUpdater linkupCountUpdater;

    @Autowired
    public void init() {
        progressUpdater.setListener((oes) -> {
            int year = oes.get(0).getYear();
            int season = oes.get(0).getSeason();
            int week = ProgressUtil.week(year, season);
            collectWeekProgress(oes, year, season, week);
        });

        linkupCountUpdater.setListener((containerType, containerId, year, season) -> {
            List<ObjectiveEntity> oes = objectiveRepository.findAllByContainerTypeAndContainerIdAndYearAndSeason(
                    containerType,
                    containerId,
                    year,
                    season
            );
            collectLinkupCount(oes, year, season);
        });
    }


    @Override
    public void collectData(int year, int season, int week) {
        List<ObjectiveEntity> oes = objectiveRepository.findAllByYearAndSeason(year, season);
        collectWeekProgress(oes, year, season, week);
        collectLinkupCount(oes, year, season);
    }


    @Override
    public LinkedCount getLinkedCount(Integer container, Integer containerId, ParamParser.YearSeason ys) {
        if (container == ObjectiveContainer.COMPANY) {
            return getCompanyLinkedCount(container, containerId, ys);
        }
        if (container == ObjectiveContainer.TEAM) {
            return getTeamLinkedCount(container, containerId, ys);
        }
        return getUserLinkedCount(container, containerId, ys);
    }

    private List<ObjectiveWeekProgressEntity> getUserWeekProgress(Integer container, Integer id, ParamParser.YearSeason ys) throws InstantiationException, IllegalAccessException {
        List<ObjectiveWeekProgressEntity> owpes = null;
        if (ys.getSeason() == null) {
            owpes = objectiveWeekProgressRepository.findAllYearSeasonWeekProgress(container, id, ys.getYear());
        } else {
            owpes = objectiveWeekProgressRepository.findByYearSeasonWeekProgress(container, id, ys.getYear(), ys.getSeason());
        }
        return owpes;
    }

    private List<ObjectiveWeekProgressEntity> getTeamWeekProgress(Integer container, Integer id, ParamParser.YearSeason ys) throws InstantiationException, IllegalAccessException {
        List<ObjectiveWeekProgressEntity> owpes = null;
        if (ys.getSeason() == null) {
            owpes = objectiveWeekProgressRepository.findTeamUserWeekProgress(id, ys.getYear());
            owpes.addAll(objectiveWeekProgressRepository.findAllContainerIdYearWeekProgress(container, id, ys.getYear(), SessionUtil.currentMu()));
        } else {
            owpes = objectiveWeekProgressRepository.findTeamUserWeekProgress(id, ys.getYear(), ys.getSeason());
            owpes.addAll(objectiveWeekProgressRepository.findByYearSeasonWeekProgress(container, id, ys.getYear(), ys.getSeason()));
        }

        return owpes;
    }

    private List<ObjectiveWeekProgressEntity> getCompanyWeekProgress(Integer container, Integer id, ParamParser.YearSeason ys) throws InstantiationException, IllegalAccessException {
        List<ObjectiveWeekProgressEntity> owpes = null;
        if (ys.getSeason() == null) {
            owpes = objectiveWeekProgressRepository.findAllYearWeekProgress(ys.getYear(), SessionUtil.currentMu());
        } else {
            owpes = objectiveWeekProgressRepository.findAllYearSeasonWeekProgress(ys.getYear(), ys.getSeason(), SessionUtil.currentMu());
        }
        return owpes;
    }

    @Override
    public List<WeekProgress> getObjectiveWeekProgress(Integer container, Integer id, ParamParser.YearSeason ys) throws IllegalAccessException, InstantiationException {
        List<ObjectiveWeekProgressEntity> owpes = null;
        if (container == ObjectiveContainer.COMPANY) {
            owpes = getCompanyWeekProgress(container, id, ys);
        } else if (container == ObjectiveContainer.TEAM) {
            owpes = getTeamWeekProgress(container, id, ys);
        } else {
            owpes = getUserWeekProgress(container, id, ys);
        }
        List<WeekProgress> wps = BeanUtil.fillList(
                owpes,
                WeekProgress.class,
                (s, t) -> {
                    ((WeekProgress) t).setObjId(((ObjectiveWeekProgressEntity) s).getObjId().toString());
                    ((WeekProgress) t).setProgress(Math.round(((WeekProgress) t).getProgress()) * 1.0);
                });
        return wps;
    }

    List<Integer[]> getSeasonUserStatus(List<Integer> uids, Integer year, Integer season) {

        List<Integer> objIds = objectiveRepository.getObjectiveIds(3, uids, year, season);
        if (!objIds.isEmpty()) {
            return objectiveWeekProgressRepository.getSeasonObjectiveStatusCount(objIds);
        }
        return Collections.emptyList();

    }

    List<Integer[]> getSeasonTeamStatus(List<Integer> tids, Integer year, Integer season) {
        List<Integer[]> status = new ArrayList<>();
        List<Integer> objIds = objectiveRepository.getObjectiveIds(2, tids, year, season);
        if (!objIds.isEmpty()) {
            status.addAll(objectiveWeekProgressRepository.getSeasonObjectiveStatusCount(objIds));
        }
        List<Integer> ids = userRepository.findIdsByTeams(tids);
        if (!ids.isEmpty()) {
            status.addAll(getSeasonUserStatus(ids, year, season));
        }
        return status;
    }

    List<Integer[]> getSeasonCompanyStatus(Integer year, Integer season) {
        List<Integer[]> status = new ArrayList<>();
        List<Integer> objIds = objectiveRepository.getCompanyObjectiveIds(SessionUtil.currentMu(), year, season);
        if (!objIds.isEmpty()) {
            status.addAll(objectiveWeekProgressRepository.getSeasonObjectiveStatusCount(objIds));
        }
        List<Integer> ids = teamRepository.findIds(SessionUtil.currentMu());
        if (!ids.isEmpty()) {
            status.addAll(getSeasonTeamStatus(ids, year, season));
        }
        return status;
    }

    private StatusCount getSeasonStatusCount(Integer container, Integer id, ParamParser.YearSeason ys) {
        List<Integer[]> status = new ArrayList<>();
        switch (container) {
            case ObjectiveContainer.COMPANY:
                status = getSeasonCompanyStatus(ys.getYear(), ys.getSeason());
                break;
            case ObjectiveContainer.TEAM:
                status = getSeasonTeamStatus(Arrays.asList(id), ys.getYear(), ys.getSeason());
                break;
            case ObjectiveContainer.USER:
                status = getSeasonUserStatus(Arrays.asList(id), ys.getYear(), ys.getSeason());
                break;
        }

        StatusCount sc = new StatusCount();
        for (Integer[] row : status) {
            if (null != row) {
                sc.setOffTrackCount(sc.getOffTrackCount() + toZero(row[0]));
                sc.setAtRiskCount(sc.getAtRiskCount() + toZero(row[1]));
                sc.setOnTrackCount(sc.getOnTrackCount() + toZero(row[2]));
                sc.setExceededCount(sc.getExceededCount() + toZero(row[3]));
            }
        }

        return sc;

    }

    private StatusCount getYearStatusCount(Integer container, Integer id, ParamParser.YearSeason ys) {
        StatusCount sc = new StatusCount();
        for (int i = 0; i < 4; ++i) {
            StatusCount seasonStatusCount = getSeasonStatusCount(container, id, new ParamParser.YearSeason(ys.getYear(), i));

            sc.setOffTrackCount(sc.getOffTrackCount() + seasonStatusCount.getOffTrackCount());
            sc.setAtRiskCount(sc.getAtRiskCount() + seasonStatusCount.getAtRiskCount());
            sc.setOnTrackCount(sc.getOnTrackCount() + seasonStatusCount.getOnTrackCount());
            sc.setExceededCount(sc.getExceededCount() + seasonStatusCount.getExceededCount());
        }
        return sc;
    }


    @Override
    public StatusCount getStatusCount(Integer container, Integer id, ParamParser.YearSeason ys) {
        if (ys.getSeason() != null) {
            return getSeasonStatusCount(container, id, ys);
        } else {
            return getYearStatusCount(container, id, ys);
        }
    }

    private WeekProcessCompositeCategory getCompanyWPCC(ParamParser.YearSeason ys) {
        WeekProcessCompositeCategory wpcc = new WeekProcessCompositeCategory();
        List<ObjectiveWeekProgressEntity> cowpes = null;
        List<ObjectiveWeekProgressEntity> towpes = null;
        List<ObjectiveWeekProgressEntity> uowpes = null;
        if (ys.getSeason() == null) {
            cowpes = objectiveWeekProgressRepository.findAllContainerYearWeekProgress(ObjectiveContainer.COMPANY, ys.getYear(), SessionUtil.currentMu());
            towpes = objectiveWeekProgressRepository.findAllContainerYearWeekProgress(ObjectiveContainer.TEAM, ys.getYear(), SessionUtil.currentMu());
            uowpes = objectiveWeekProgressRepository.findAllContainerYearWeekProgress(ObjectiveContainer.USER, ys.getYear(), SessionUtil.currentMu());
        } else {
            cowpes = objectiveWeekProgressRepository.findAllContainerYearSeasonWeekProgress(ObjectiveContainer.COMPANY, ys.getYear(), ys.getSeason(), SessionUtil.currentMu());
            towpes = objectiveWeekProgressRepository.findAllContainerYearSeasonWeekProgress(ObjectiveContainer.TEAM, ys.getYear(), ys.getSeason(), SessionUtil.currentMu());
            uowpes = objectiveWeekProgressRepository.findAllContainerYearSeasonWeekProgress(ObjectiveContainer.USER, ys.getYear(), ys.getSeason(), SessionUtil.currentMu());
        }

        wpcc.setCompany(ProgressUtil.completeWeekProgress(ProgressUtil.computeCompositeWeekProgress(cowpes), ys, null));
        wpcc.setTeam(ProgressUtil.completeWeekProgress(ProgressUtil.computeCompositeWeekProgress(towpes), ys, null));
        wpcc.setPerson(ProgressUtil.completeWeekProgress(ProgressUtil.computeCompositeWeekProgress(uowpes), ys, null));
        return wpcc;
    }

    private WeekProcessCompositeCategory getTeamWPCC(Integer teamId, ParamParser.YearSeason ys) {
        WeekProcessCompositeCategory wpcc = new WeekProcessCompositeCategory();
        List<ObjectiveWeekProgressEntity> towpes = null;
        List<ObjectiveWeekProgressEntity> uowpes = null;
        if (ys.getSeason() == null) {
            towpes = objectiveWeekProgressRepository.findAllTeamYearWeekProgress(teamId, ys.getYear());
            uowpes = objectiveWeekProgressRepository.findAllTeamUserYearWeekProgress(teamId, ys.getYear());
        } else {
            towpes = objectiveWeekProgressRepository.findAllTeamYearSeasonWeekProgress(teamId, ys.getYear(), ys.getSeason());
            uowpes = objectiveWeekProgressRepository.findAllTeamUserYearSeasonWeekProgress(teamId, ys.getYear(), ys.getSeason());
        }
        wpcc.setTeam(ProgressUtil.completeWeekProgress(ProgressUtil.computeCompositeWeekProgress(towpes), ys, null));
        wpcc.setPerson(ProgressUtil.completeWeekProgress(ProgressUtil.computeCompositeWeekProgress(uowpes), ys, null));
        return wpcc;
    }

    private WeekProcessCompositeCategory getPersonWPCC(Integer uid, ParamParser.YearSeason ys) {
        WeekProcessCompositeCategory wpcc = new WeekProcessCompositeCategory();
        List<ObjectiveWeekProgressEntity> uowpes = null;
        if (ys.getSeason() == null) {
            uowpes = objectiveWeekProgressRepository.findAllUserYearWeekProgress(uid, ys.getYear());
        } else {
            uowpes = objectiveWeekProgressRepository.findAllUserYearSeasonWeekProgress(uid, ys.getYear(), ys.getSeason());
        }
        wpcc.setPerson(ProgressUtil.completeWeekProgress(ProgressUtil.computeCompositeWeekProgress(uowpes), ys, null));
        return wpcc;
    }

    @Override
    public WeekProcessCompositeCategory getCompositeProgress(Integer container, Integer id, ParamParser.YearSeason ys) throws IllegalAccessException, InstantiationException {
        WeekProcessCompositeCategory wpcc = null;
        if (container == ObjectiveContainer.COMPANY) {
            wpcc = getCompanyWPCC(ys);
        } else if (container == ObjectiveContainer.TEAM) {
            wpcc = getTeamWPCC(id, ys);
        } else {
            wpcc = getPersonWPCC(id, ys);
        }
        return wpcc;
    }

    @Override
    public LatestWeekProcessMap getLatestProgress(ParamParser.YearSeason ys) {
        List<ObjectiveWeekProgressEntity> lastWps = null;
        if (ys.getSeason() == null) {
            lastWps = objectiveWeekProgressRepository.findAllLatestYearWeekProgress(ys.getYear(), SessionUtil.currentMu());
        } else {
            lastWps = objectiveWeekProgressRepository.findAllLatestYearSeasonWeekProgress(ys.getYear(), ys.getSeason(), SessionUtil.currentMu());
        }


        //group Map 结构
        // + COMPANY
        //       ID1 -> week progresses
        //       ID2 -> week progresses
        //       ...
        // + TEAM
        //       ID1 -> week progresses
        //       ID2 -> week progresses
        //       ...
        // + USER
        //       ID1 -> week progresses
        //       ID2 -> week progresses
        //       ...
        Map<Integer, Map<Integer, List<ObjectiveWeekProgressEntity>>> groupMap = new HashMap<>();
        for (ObjectiveWeekProgressEntity owpe : lastWps) {
            ObjectiveEntity oe = objectiveRepository.findOneById(owpe.getObjId());
            if (!groupMap.containsKey(oe.getContainerType())) {
                groupMap.put(oe.getContainerType(), new HashMap<>());
            }

            if (!groupMap.get(oe.getContainerType()).containsKey(oe.getContainerId())) {
                groupMap.get(oe.getContainerType()).put(oe.getContainerId(), new ArrayList<>());
            }
            groupMap.get(oe.getContainerType()).get(oe.getContainerId()).add(owpe);
        }

        LatestWeekProcessMap lwpMap = new LatestWeekProcessMap();
        for (Map.Entry<Integer, Map<Integer, List<ObjectiveWeekProgressEntity>>> g : groupMap.entrySet()) {
            for (Map.Entry<Integer, List<ObjectiveWeekProgressEntity>> c : g.getValue().entrySet()) {
                if (g.getKey().equals(ObjectiveContainer.COMPANY)) {
                    lwpMap.getCompany().put(c.getKey().toString(), ProgressUtil.computeCompositeProgress(c.getValue()));
                } else if (g.getKey().equals(ObjectiveContainer.TEAM)) {
                    lwpMap.getTeam().put(c.getKey().toString(), ProgressUtil.computeCompositeProgress(c.getValue()));
                } else {
                    lwpMap.getPerson().put(c.getKey().toString(), ProgressUtil.computeCompositeProgress(c.getValue()));
                }
            }
        }

        return lwpMap;
    }

    private LinkedCount getUserLinkedCount(Integer container, Integer containerId, ParamParser.YearSeason ys) {
        List<Integer[]> ret = objectiveLinkCountRepository.sumOneUserObjectiveLinkupCount(containerId, ys.getYear(), ys.getSeason());
        LinkedCount lc = new LinkedCount();
        lc.setTeamLinkedCount(toZero(ret.get(0)[0]));
        lc.setTeamNotLinkedCount(toZero(ret.get(0)[1]));
        return lc;
    }

    private Integer toZero(Integer val) {
        return val == null ? 0 : val;
    }

    private LinkedCount getTeamLinkedCount(Integer container, Integer teamId, ParamParser.YearSeason ys) {
        List<Integer[]> ret = objectiveLinkCountRepository.sumOneTeamObjectiveLinkupCount(teamId, ys.getYear(), ys.getSeason());
        LinkedCount lc = new LinkedCount();
        lc.setTeamLinkedCount(toZero(ret.get(0)[0]));
        lc.setTeamNotLinkedCount(toZero(ret.get(0)[1]));

        ret = objectiveLinkCountRepository.sumAllUserObjectiveLinkupCount(teamId, ys.getYear(), ys.getSeason());
        lc.setPersonLinkedCount(toZero(ret.get(0)[0]));
        lc.setPersonNotLinkedCount(toZero(ret.get(0)[1]));
        return lc;
    }

    private LinkedCount getCompanyLinkedCount(Integer container, Integer compId, ParamParser.YearSeason ys) {
        List<Integer[]> ret = objectiveLinkCountRepository.sumAllTeamObjectiveLinkupCount(ys.getYear(), ys.getSeason(), SessionUtil.currentMu());
        LinkedCount lc = new LinkedCount();
        lc.setTeamLinkedCount(toZero(ret.get(0)[0]));
        lc.setTeamNotLinkedCount(toZero(ret.get(0)[1]));
        ret = objectiveLinkCountRepository.sumAllTeamUserObjectiveLinkupCount(ys.getYear(), ys.getSeason(), SessionUtil.currentMu());
        lc.setPersonLinkedCount(toZero(ret.get(0)[0]));
        lc.setPersonNotLinkedCount(toZero(ret.get(0)[1]));
        return lc;
    }

    class LinkupCount {
        public Integer linked = 0;
        public Integer notLinked = 0;
        public Integer mu;
    }

    void collectLinkupCount(List<ObjectiveEntity> oes, int year, int season) {
        Map<Integer, Map<Integer, LinkupCount>> countMap = new HashMap<>();
        for (ObjectiveEntity oe : oes) {
            if (!countMap.containsKey(oe.getContainerType())) {
                countMap.put(oe.getContainerType(), new HashMap<>());
            }
            Map<Integer, LinkupCount> val = countMap.get(oe.getContainerType());
            if (!val.containsKey(oe.getContainerId())) {
                val.put(oe.getContainerId(), new LinkupCount());
            }

            LinkupCount lc = val.get(oe.getContainerId());
            lc.mu = oe.getMu();
            if (oe.getLinkAbove() != null) {
                ++lc.linked;
            } else {
                ++lc.notLinked;
            }
        }

        for (Map.Entry<Integer, Map<Integer, LinkupCount>> entry : countMap.entrySet()) {
            for (Map.Entry<Integer, LinkupCount> linkEntry : entry.getValue().entrySet()) {
                ObjectiveLinkCountEntity olce = objectiveLinkCountRepository.findByYearAndSeasonAndContainerTypeAndContainerId(year, season, entry.getKey(), linkEntry.getKey());
                if (olce == null) {
                    olce = new ObjectiveLinkCountEntity();
                    olce.setContainerType(entry.getKey());
                    olce.setContainerId(linkEntry.getKey());
                    olce.setYear(year);
                    olce.setSeason(season);
                    olce.setMu(linkEntry.getValue().mu);
                    olce.setCreateTime(new Timestamp(System.currentTimeMillis()));
                }
                olce.setLinkupCount(linkEntry.getValue().linked);
                olce.setNotLinkupCount(linkEntry.getValue().notLinked);
                olce.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                objectiveLinkCountRepository.saveAndFlush(olce);
            }
        }
    }

    private void collectWeekProgress(List<ObjectiveEntity> oes, int year, int season, int week) {
        List<ObjProgress> ops = ProgressUtil.computeProgresses(oes, week, true);
        int i = -1;
        for (ObjProgress op : ops) {
            ++i;
            Integer id = Integer.valueOf(op.getObjectiveId());
            if (objectiveRepository.findById(id).isPresent()) {
                ObjectiveWeekProgressEntity owpe = objectiveWeekProgressRepository.findByYearAndSeasonAndWeekAndObjId(year, season, week, id);
                if (owpe == null) {
                    owpe = new ObjectiveWeekProgressEntity();
                    owpe.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    owpe.setObjId(Integer.valueOf(op.getObjectiveId()));
                    owpe.setYear(year);
                    owpe.setSeason(season);
                    owpe.setWeek(week);
                    owpe.setMu(oes.get(i).getMu());
                }
                owpe.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                owpe.setProgress(op.getProgress());
                owpe.setExpected(op.getExpected());
                owpe.setStatus(op.getStatus());
                objectiveWeekProgressRepository.saveAndFlush(owpe);
            } else {
                ObjectiveWeekProgressEntity owpe = objectiveWeekProgressRepository.findByYearAndSeasonAndWeekAndObjId(year, season, week, id);
                if (null != owpe) {
                    objectiveWeekProgressRepository.delete(owpe);
                    objectiveWeekProgressRepository.flush();
                }
            }
        }
    }
}
