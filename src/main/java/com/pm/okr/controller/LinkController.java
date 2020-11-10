package com.pm.okr.controller;

import com.pm.okr.common.ObjectiveMapper;
import com.pm.okr.common.ProgressUtil;
import com.pm.okr.controller.vo.ObjProgress;
import com.pm.okr.controller.vo.Objective;
import com.pm.okr.controller.vo.Result;
import com.pm.okr.controller.vo.ResultKRChanged;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.services.link.LinkService;
import com.pm.okr.services.session.SessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CrossOrigin
@Api(tags = "Objective 链接控制")
@Controller
@RequestMapping("/link")
public class LinkController {


    @Autowired
    SessionService sessionService;

    @Autowired
    LinkService linkService;

    @ApiOperation(
            value = "创建向上链接",
            notes = "前置条件：</br>" +
                    "<span style='margin-left: 2em;'>1、链接起始端级别不低于终止端</br>" +
                    "<span style='margin-left: 2em;'>2、不能出现环状链接</br>" +
                    "链接成功后，返回受影响的Objective进度（不包括起始端Objective）")
    @PostMapping(value = "/above", produces = "application/json")
    @ResponseBody
    public Result<List<ObjProgress>, String> addLink(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "起始 Objective ID", required = true)
            @RequestParam String from,
            @ApiParam(value = "目标 Objective ID", required = true)
            @RequestParam String to) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<ObjectiveEntity> oes = linkService.linkObjective(Integer.valueOf(from), Integer.valueOf(to));
            if (oes != null){
                return Result.ok(ProgressUtil.computeProgresses(oes, false));
            }
            return Result.fail("链接创建失败");
        }));
    }

    @ApiOperation(
            value = "删除向上链接",
            notes = "返回进度受影响的Objective列表")
    @DeleteMapping(value = "above/{id}", produces = "application/json")
    @ResponseBody
    public Result<List<ObjProgress>, String> removeLink(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "起始 Objective ID", required = true)
            @PathVariable String id) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<ObjectiveEntity> oes = linkService.breakLinkAbove(Integer.valueOf(id));
            if (oes != null){
                return Result.ok(ProgressUtil.computeProgresses(oes, false));
            }
            return Result.fail("断开链接失败");
        }));

    }

    @ApiOperation(
            value = "删除向下链接",
            notes = "如果起始 Objective 通过 Keyresult assign 生成的，则返回新的KeyResult, 以及进度受影响的Objective列表，并删起始Objective<br/>" +
                    "否则直接断开链接，并返回进度受影响的 Objective 列表")
    @DeleteMapping(value = "below/{id}", produces = "application/json")
    @ResponseBody
    public Result<ResultKRChanged, String> removeLinkBelow(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "起始 Objective ID", required = true)
            @PathVariable String id) throws Exception {
        return sessionService.protect(sid, (session -> {
            ResultKRChanged ret = linkService.breakLinkBelow(Integer.valueOf(id));
            if (ret != null){
                return Result.ok(ret);
            }
            return Result.fail("断开链接失败");
        }));
    }

    @Getter
    @Setter
    class LinkObjectives {
        List<Objective.PartToLink> companyObjectives = new ArrayList<>();
        List<Objective.PartToLink> teamObjectives = new ArrayList<>();
        List<Objective.PartToLink> userObjectives = new ArrayList<>();
    }

    @ApiOperation(
            value = "获取可链接 Objectives",
            notes = "获取可供 Objective ID 指定的 Objective 进行链接的Objectives")
    @GetMapping(value = "objectives/{id}", produces = "application/json")
    @ResponseBody
    public Result<LinkObjectives, String> getLinkObject(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective ID", required = true)
            @PathVariable String id) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<Objective.PartToLink> objs = linkService.getLinkObjectives(Integer.valueOf(id));
            LinkObjectives los = new LinkObjectives();
            for (Objective.PartToLink p : objs){
                if (p.getOwner().getCompany() != null){
                    los.getCompanyObjectives().add(p);
                } else if (p.getOwner().getTeam() != null){
                    los.getTeamObjectives().add(p);
                } else if (p.getOwner().getUser() != null){
                    los.getUserObjectives().add(p);
                }
            }
            return Result.ok(los);
        }));
    }
}
