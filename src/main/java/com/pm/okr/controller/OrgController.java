package com.pm.okr.controller;

import com.pm.okr.common.BeanUtil;
import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.CompanyEntity;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;
import com.pm.okr.services.org.OrgService;
import com.pm.okr.services.session.SessionService;
import com.pm.okr.services.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@Api(tags = "组织架构管理")
@Controller
@RequestMapping("/org")
public class OrgController {


    @Autowired
    SessionService sessionService;

    @Autowired
    OrgService orgService;

    @Autowired
    UserService userService;

    @ApiOperation(
            value = "添加Team",
            notes = "新建Team，返回新建后的Team ID<br/>" +
                    "如果Team 名已经存在，则返回错误码及TeamID")
    @PostMapping(value = "team", produces = "application/json")
    @ResponseBody
    public Result<String, String> addTeam(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "团队(Team.PartAdd)", required = true)
            @RequestBody Team.PartAdd team) throws Exception {
        return sessionService.protect(sid, (session -> {
            if (team.getName() == null || team.getName().isEmpty()){
                return Result.fail(ErrorCode.FAILED, "队名不能为空");
            }
            Integer id = orgService.addTeam(team);
            if (null == id) {
                return Result.fail(ErrorCode.TEAM_EXISTS, "队名已存在");
            }
            return Result.ok(id);
        }));
    }

    @ApiOperation(
            value = "删除Team",
            notes = "成功返回OK, 如果Team存在成员，则删除失败")
    @DeleteMapping(value = "team/{id}", produces = "application/json")
    @ResponseBody
    public Result<String, String> removeTeam(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Team ID", required = true)
            @PathVariable String id) throws Exception {
        return sessionService.protect(sid, (session -> {
            if (orgService.removeTeam(Integer.valueOf(id))) {
                return Result.ok();
            }
            return Result.fail(ErrorCode.FAILED, "Team 存在成员,无法删除");
        }));

    }

    @ApiOperation(
            value = "修改Team信息",
            notes = "成功返回OK，失败返回错误及重名TeamID")
    @PutMapping(value = "team", produces = "application/json")
    @ResponseBody
    public Result<String, String> renameTeam(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Team(Team.Part)", required = true)
            @RequestBody Team.Part team) throws Exception {
        return sessionService.protect(sid, (session -> {
            if (team.getId() == null){
                return Result.fail(ErrorCode.FAILED, "Team ID 不能为空");
            }
            Integer duplicationNameId = orgService.updateTeam(team);
            if (null != duplicationNameId){
                return Result.fail(ErrorCode.FAILED, "Team name 已存在 ：" + duplicationNameId);
            }
            return Result.ok();
        }));
    }


    @ApiOperation(
            value = "为Team添加用户",
            notes = "成功返回新加用户")
    @PostMapping(value = "team/{tId}/user/{uId}", produces = "application/json")
    @ResponseBody
    public Result<User.PartDetail, String> addTeamUser(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "team ID", required = true)
            @PathVariable String tId,
            @ApiParam(value = "User ID", required = true)
            @PathVariable String uId) throws Exception {
        return sessionService.protect(sid, (session -> {
            UserEntity ue = orgService.addTeamUser(Integer.valueOf(tId), Integer.valueOf(uId));
            if (null != ue){
                List<TeamEntity> tes = userService.getTeam(Integer.valueOf(ue.getId()));

                User.PartDetail detail = BeanUtil.fill(ue, new User.PartDetail());
                detail.setTeam(BeanUtil.fillList(tes, Team.Part.class));

                return Result.ok(BeanUtil.fill(ue, detail));
            }
            return Result.fail(ErrorCode.FAILED, "用户不存在");
        }));
    }

    @ApiOperation(
            value = "为Team移除用户",
            notes = "成功返回删除的用户")
    @DeleteMapping(value = "team/{tId}/user/{uId}", produces = "application/json")
    @ResponseBody
    public Result<String, String> removeTeamUser(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "team ID", required = true)
            @PathVariable String tId,
            @ApiParam(value = "User ID", required = true)
            @PathVariable String uId) throws Exception {
        return sessionService.protect(sid, (session -> {
            orgService.removeTeamUser(Integer.valueOf(tId), Integer.valueOf(uId));
            return Result.ok();
        }));
    }

    @ApiOperation(
            value = "获取公司组织结构",
            notes = "返回以公司为跟元素的组织结构")
    @GetMapping(value = "company", produces = "application/json")
    @ResponseBody
    public Result<Company, String> getOrgs(
            @ApiParam(value = "会话ID", required = true)
            @RequestHeader String sid) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<TeamEntity> teams = orgService.getTeams();
            List<Team> vteams = new ArrayList<>();
            for (TeamEntity t : teams){
                Team vt = BeanUtil.fill(t, new Team());
                vt.setManagers(new ArrayList<>());
                for (UserEntity ue : t.getManagers()){
                    vt.getManagers().add(ue.getId().toString());
                }
                vt.setObservers(new ArrayList<>());
                for (UserEntity ue : t.getObservers()){
                    vt.getObservers().add(ue.getId().toString());
                }

                vt.setMembers(new ArrayList<>());
                for (UserEntity ue : t.getMembers()){
                    vt.getMembers().add(BeanUtil.fill(ue, new User.PartDetail()));
                }
                vteams.add(vt);
            }
            CompanyEntity comp = orgService.getCompany();
            Company vComp = BeanUtil.fill(comp, new Company());
            vComp.setTeams(vteams);
            return Result.ok(vComp);
        }));
    }

}
