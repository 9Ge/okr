package com.pm.okr.controller;

import com.pm.okr.common.ObjectiveMapper;
import com.pm.okr.common.ProgressUtil;
import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.services.objective.ObjectiveService;
import com.pm.okr.services.org.OrgService;
import com.pm.okr.services.session.SessionService;
import com.pm.okr.services.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.pm.okr.common.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CrossOrigin
@Api(tags = "Objective 管理")
@Controller
@RequestMapping("/objective")
public class ObjectiveController {

    @Autowired
    SessionService sessionService;

    @Autowired
    ObjectiveService objectiveService;

    @Autowired
    OrgService orgService;

    @Autowired
    UserService userService;

    @ApiOperation(
            value = "添加Objective",
            notes = "Objective 添加成功后返回 Objective 基本信息")
    @PostMapping(value = "", produces = "application/json")
    @ResponseBody
    public Result<List<Objective.PartAdd>, String> addObjective(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "1:公司; 2:Team; 3:User", required = true)
            @RequestParam Integer container,
            @ApiParam(value = "公司/Team/User ID", required = true)
            @RequestParam String id,
            @ApiParam(value = "年季 eg.20191(2019年一季度)", required = true)
            @RequestParam String yearSeason,
            @ApiParam(value = "是否分配给所有人")
            @RequestParam(defaultValue = "false", required = false) Boolean everyone,
            @ApiParam(value = "objective 内容", required = true)
            @RequestParam String content,
            @ApiParam(value = "objective 颜色")
            @RequestParam(required = false) String color) throws Exception {
        return sessionService.protect(sid, (session -> {
            ParamParser.YearSeason ys = ParamParser.getYearSeason(yearSeason);
            List<ObjectiveEntity> oes;
            if (ys.getSeason() == null){
                oes = new ArrayList<>();
                for (int i = 0; i < 4; ++i){
                    oes.addAll(objectiveService.addObjective(
                            container,
                            Integer.valueOf(id),
                            ys.getYear(),
                            i,
                            everyone,
                            content,
                            color));
                }

            }else{
                oes = objectiveService.addObjective(
                        container,
                        Integer.valueOf(id),
                        ys.getYear(),
                        ys.getSeason(),
                        everyone,
                        content,
                        color);

            }

            if (oes == null) {
                return Result.fail(ErrorCode.FAILED, "objective 添加失败");
            }


            List<Objective.PartAdd> addObjs = ObjectiveMapper.toPartAdds(oes, objectiveService.getEntityLoader());
            return Result.ok(addObjs);
        }));
    }

    @ApiOperation(
            value = "修改Objective内容",
            notes = "修改成功，返回'OK'")
    @PutMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public Result<String, String> updateOkrContent(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective ID", required = true)
            @PathVariable String id,
            @ApiParam(value = "objective 内容", required = true)
            @RequestParam String content,
            @ApiParam(value = "objective 颜色")
            @RequestParam(required = false) String color) throws Exception {
        return sessionService.protect(sid, (session -> {
            objectiveService.updateContent(Integer.valueOf(id), content, color);
            return Result.ok();
        }));

    }

    @ApiOperation(
            value = "修改 Objective 权重",
            notes = "修改成功，返回相关Obj最新进度信息",
            hidden = true)
    @PutMapping(value = "/{id}/weight/{weight}", produces = "application/json")
    @ResponseBody
    public Result<List<ObjProgress>, String> updateObjectiveWeight(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective ID", required = true)
            @PathVariable String id,
            @ApiParam(value = "权重", required = true)
            @PathVariable Double weight) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<ObjectiveEntity> objs = objectiveService.updateWeight(Integer.valueOf(id), weight);
            return Result.ok(ProgressUtil.computeProgresses(objs, false));
        }));
    }


    @ApiOperation(
            value = "获取 Objectives",
            notes = "")
    @GetMapping(value = "ids", produces = "application/json")
    @ResponseBody
    public Result<List<Objective>, String> getObjective(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective IDs， 以逗号分隔", required = true)
            @RequestParam Integer[] ids) throws Exception {
        return sessionService.protect(sid, (session -> {
            return Result.ok(objectiveService.getObjectives(Arrays.asList(ids)));
        }));
    }

    @ApiOperation(
            value = "获取年/季度 Objectives",
            notes = "如果未指定季度，则返回全年Objective。<br/>" +
                    "withChildren 为true时， 该接口返回该容器下所有Objectives")
    @GetMapping(value = "", produces = "application/json")
    @ResponseBody
    public Result<ObjectiveNode, String> getSeasonObjective(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "1:公司; 2:Team; 3:User", required = true)
            @RequestParam Integer container,
            @ApiParam(value = "公司/Team/User ID", required = true)
            @RequestParam String id,
            @ApiParam(value = "是否包含下级Objective", required = false, defaultValue = "true")
            @RequestParam(required = false, defaultValue = "false") Boolean withChildren,
            @ApiParam(value = "年份(季度)   eg. <br/>19年：2019 <br/>19年第一季度：20191", required = true)
            @RequestParam String yearSeason,
            @ApiParam(value = "filter 进度 0:0~100%; 1:0~33%; 2:34~66%; 3:67~100%", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer range,
            @ApiParam(value = "filter 状态 0:ALL; 1:OFF TRACK; 2:AT RISK; 3:ON TRACK; 4:EXCEEDED;", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer status,
            @ApiParam(value = "filter KeyResult 0:All; 1:2周前; 2:4周前; 3:8周前", required = false)
            @RequestParam(required = false, defaultValue = "0") Integer krs) throws Exception {
        return sessionService.protect(sid, (session -> {
            ParamParser.YearSeason ys = ParamParser.getYearSeason(yearSeason);
            return Result.ok(objectiveService.getObjectiveNode(
                    container,
                    Integer.valueOf(id),
                    withChildren,
                    ys.getYear(),
                    ys.getSeason(),
                    new ObjectiveFilter(range, status, krs)));
        }));
    }


    @ApiOperation(
            value = "删除 Objective",
            notes = "返回受影响Objective的进度")
    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public Result<List<ObjProgress>, String> removeObjective(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective ID", required = true)
            @PathVariable String id) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<ObjectiveEntity> oes = objectiveService.removeObjective(Integer.valueOf(id));

            return Result.ok(ProgressUtil.computeProgresses(oes, false));
        }));
    }

    @ApiOperation(
            value = "拷贝/移动 Objective",
            notes = "拷贝/移动 Objective 到 公司/Team/User, 返回新的Objective<br/>" +
                    "如果objective已经有上级链接则不允许移动<br/>" +
                    "拷贝不会拷贝链接信息")
    @PostMapping(value = "transaction", produces = "application/json")
    @ResponseBody
    public Result<Objective, String> copyOrMove(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "true : 拷贝 Objective; false : 移动Objective", required = true)
            @RequestParam Boolean copy,
            @ApiParam(value = "Objective ID", required = true)
            @RequestParam String oId,
            @ApiParam(value = "Objective 容器类型，1:公司; 2:Team; 3:用户", required = true)
            @RequestParam Integer container,
            @ApiParam(value = "Objective 容器 ID", required = true)
            @RequestParam String cId) throws Exception {
        return sessionService.protect(sid, (session -> {
            Pair<ObjectiveEntity, ObjectiveEntity> oe = null;
            if (copy){
                oe = objectiveService.copyObjectiveTo(Integer.valueOf(oId), container, Integer.valueOf(cId));
            }else{
                oe = objectiveService.moveObjectiveTo(Integer.valueOf(oId), container, Integer.valueOf(cId));
            }
            if (oe != null) {
                return Result.ok(ObjectiveMapper.from(oe.getValue(), objectiveService.getEntityLoader(), null));
            }
            return Result.fail(ErrorCode.FAILED, "转移失败");
        }));

    }

    @ApiOperation(
            value = "Objective 归档",
            notes = "成功返回OK")
    @PostMapping(value = "archive/{oId}", produces = "application/json")
    @ResponseBody
    public Result<String, String> archive(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective ID", required = true)
            @PathVariable String oId) throws Exception {
        return sessionService.protect(sid, (session -> {
            objectiveService.achive(Integer.valueOf(oId));
            return Result.ok();
        }));

    }

    @ApiOperation(
            value = "取消 Objective 归档",
            notes = "成功返回OK")
    @DeleteMapping(value = "archive/{oId}", produces = "application/json")
    @ResponseBody
    public Result<String, String> unarchive(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective ID", required = true)
            @PathVariable String oId) throws Exception {
        return sessionService.protect(sid, (session -> {
            objectiveService.unachive(Integer.valueOf(oId));
            return Result.ok();
        }));

    }
}
