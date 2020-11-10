package com.pm.okr.services.org;

import com.pm.okr.common.BeanUtil;
import com.pm.okr.common.ObjectiveMapper;
import com.pm.okr.common.SessionUtil;
import com.pm.okr.controller.vo.ObjectiveContainer;
import com.pm.okr.controller.vo.Session;
import com.pm.okr.controller.vo.Team;
import com.pm.okr.model.entity.CompanyEntity;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;
import com.pm.okr.model.repository.*;
import com.pm.okr.services.objective.ObjectiveService;
import com.pm.okr.services.util.ProgressUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrgServiceImpl implements OrgService {

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
    ObjectiveService objectiveService;


    @Override
    public Integer addTeam(Team.PartAdd team) {
        Integer mu = SessionUtil.currentMu();
        TeamEntity teamEntity = teamRepository.findByNameAndMu(team.getName(), mu);
        if (null != teamEntity) {
            return null;
        }

        List<CompanyEntity> comapnies = companyRepository.findAllByMu(mu);
        if (comapnies.isEmpty()) {
            return null;
        }

        teamEntity = new TeamEntity();
        BeanUtil.fill(team, teamEntity);
        teamEntity.setCompanyId(comapnies.get(0).getId());
        teamEntity.setMu(mu);
        return teamRepository.saveAndFlush(teamEntity).getId();
    }

    @Override
    public boolean removeTeam(Integer tid) {
        if (teamRepository.countMember(tid) > 0) {
            return false;
        }

        if (teamRepository.findById(tid).isPresent()) {
            List<ObjectiveEntity> oes = objectiveRepository.findAllByContainerTypeAndContainerIdAndMu(ObjectiveContainer.TEAM, tid, SessionUtil.currentMu());

            for (ObjectiveEntity oe : oes) {
                objectiveService.removeObjective(oe.getId());
            }

            teamRepository.deleteById(tid);
        }

        return true;
    }

    @Override
    public Integer updateTeam(Team.Part team) {
        TeamEntity t = teamRepository.findByIdAndMu(Integer.valueOf(team.getId()), SessionUtil.currentMu());
        if (t != null) {
            if (team.getName() != null && !team.getName().isEmpty() && !t.getName().equals(team.getName())) {
                TeamEntity t2 = teamRepository.findByNameAndMu(team.getName(), SessionUtil.currentMu());
                if (null != t2) {
                    return t2.getId();
                } else {
                    BeanUtil.fill(team, t);
                    teamRepository.saveAndFlush(t);
                }
            } else {
                BeanUtil.fill(team, t);
                teamRepository.saveAndFlush(t);
            }
        }
        return null;
    }

    @Override
    public List<Integer> addTeamManager(Integer tid, Integer uid) {
        Optional<TeamEntity> t = teamRepository.findById(tid);
        List<Integer> ret = new ArrayList<>();
        if (t.isPresent()) {
            for (UserEntity u : t.get().getManagers()) {
                ret.add(u.getId());
            }
            if (!ret.contains(uid)) {
                Optional<UserEntity> u = userRepository.findById(uid);
                if (u.isPresent()) {
                    t.get().getManagers().add(u.get());
                    teamRepository.saveAndFlush(t.get());
                    ret.add(uid);
                }
            }
        }
        return ret;
    }

    @Override
    public List<Integer> removeTeamManager(Integer tid, Integer uid) {
        Optional<TeamEntity> t = teamRepository.findById(tid);
        List<Integer> ret = new ArrayList<>();
        if (t.isPresent()) {
            UserEntity uRemove = null;
            for (UserEntity u : t.get().getManagers()) {
                if (u.getId().equals(uid)) {
                    uRemove = u;
                } else {
                    ret.add(u.getId());
                }
            }
            if (uRemove != null) {
                t.get().getManagers().remove(uRemove);
                teamRepository.saveAndFlush(t.get());
            }
        }
        return ret;
    }

    @Override
    public UserEntity addTeamUser(Integer tid, Integer uid) {
        TeamEntity t = teamRepository.findByIdAndMu(tid, SessionUtil.currentMu());
        UserEntity u = userRepository.findByIdAndMu(uid, SessionUtil.currentMu());
        if (t != null && u != null) {
            for (UserEntity ue : t.getMembers()) {
                if (ue.getId().equals(uid)) {
                    return ue;
                }
            }

            //每个用户只在一个Team中
            teamRepository.removeTeamUser(uid);

            t.getMembers().add(u);
            teamRepository.saveAndFlush(t);
        }
        return u;
    }

    @Override
    public void removeTeamUser(Integer tid, Integer uid) {
        TeamEntity t = teamRepository.findByIdAndMu(tid, SessionUtil.currentMu());

        if (t != null) {
            UserEntity uRemove = null;
            for (UserEntity u : t.getMembers()) {
                if (u.getId().equals(uid)) {
                    uRemove = u;
                }
            }
            if (uRemove != null) {
                t.getMembers().remove(uRemove);
                teamRepository.saveAndFlush(t);
            }
        }
    }

    @Override
    public List<TeamEntity> getTeams() {
        return teamRepository.findAllByMu(SessionUtil.currentMu());
    }

    @Override
    public CompanyEntity getCompany() {
        return companyRepository.findAllByMu(SessionUtil.currentMu()).get(0);
    }

    @Override
    public TeamEntity getTeam(Integer containerId) {
        return teamRepository.findByIdAndMu(containerId, SessionUtil.currentMu());
    }
}
