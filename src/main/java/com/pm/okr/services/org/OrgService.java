package com.pm.okr.services.org;

import com.pm.okr.controller.vo.Team;
import com.pm.okr.model.entity.CompanyEntity;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;

import java.util.List;


public interface OrgService {

    Integer addTeam(Team.PartAdd team);

    boolean removeTeam(Integer valueOf);

    Integer updateTeam(Team.Part team);

    List<Integer> addTeamManager(Integer tid, Integer uid);

    List<Integer> removeTeamManager(Integer tid, Integer uid);

    UserEntity addTeamUser(Integer tid, Integer uid);

    void removeTeamUser(Integer tid, Integer uid);

    List<TeamEntity> getTeams();

    CompanyEntity getCompany();

    TeamEntity getTeam(Integer containerId);

}
