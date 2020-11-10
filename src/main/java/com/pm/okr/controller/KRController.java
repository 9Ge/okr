package com.pm.okr.controller;

import com.pm.okr.common.KeyResultMapper;
import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.KeyResultEntity;
import com.pm.okr.services.keyresult.KeyResultService;
import com.pm.okr.services.session.SessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@CrossOrigin
@Api(tags = "Key Result 管理")
@Controller
@RequestMapping("/keyResult")
public class KRController {

    @Autowired
    SessionService sessionService;

    @Autowired
    KeyResultService keyResultService;

    @ApiOperation(
            value = "添加 key result",
            notes = "目标添加成功后返回目标ID及影响的Objective进度")
    @PostMapping(value = "/objective/{id}", produces = "application/json")
    @ResponseBody
    public Result addKeyResult(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "objective ID", required = true)
            @PathVariable String id,
            @ApiParam(value = "key result 内容", required = true)
            @RequestParam String content,
            @ApiParam(value = "结果类型")
            @RequestParam(required = false) String resultType,
            @ApiParam(value = "范围起始值")
            @RequestParam(required = false) Double startValue,
            @ApiParam(value = "范围结束值")
            @RequestParam(required = false) Double targetValue,
            @ApiParam(value = "保留小数位数")
            @RequestParam(required = false) Integer decimals) throws Exception {
        return sessionService.protect(sid, (session -> {
            ResultKRChanged changed = keyResultService.addKeyResult(
                    Integer.valueOf(id),
                    content,
                    resultType,
                    startValue,
                    targetValue,
                    decimals);
            return Result.ok(changed);
        }));

    }


    @ApiOperation(
            value = "修改keyResult内容",
            notes = "")
    @PutMapping(value = "/{id}/content", produces = "application/json")
    @ResponseBody
    public Result<String, String> updateContent(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "keyResult ID", required = true)
            @RequestParam String id,
            @ApiParam(value = "keyResult 内容", required = true)
            @RequestParam String content) throws Exception {
        return sessionService.protect(sid, (session -> {
            if (keyResultService.updateContent(Integer.valueOf(id), content)) {
                return Result.ok();
            }
            return Result.fail("KeyResult 不存在");
        }));

    }

    @ApiOperation(
            value = "修改 keyResult type/value",
            notes = "成功后返回最新KeyResult")
    @PutMapping(value = "/{id}/typeValue", produces = "application/json")
    @ResponseBody
    public Result<KeyResult.PartShort, String> updateTypeAndValue(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "keyResult ID", required = true)
            @PathVariable String id,
            @ApiParam(value = "结果类型")
            @RequestParam(required = false) String resultType,
            @ApiParam(value = "范围起始值")
            @RequestParam(required = false) Double startValue,
            @ApiParam(value = "范围结束值")
            @RequestParam(required = false) Double targetValue,
            @ApiParam(value = "保留小数位数")
            @RequestParam(required = false) Integer decimals) throws Exception {
        return sessionService.protect(sid, (session -> {
            KeyResultEntity kre = keyResultService.updateTypeValue(
                    Integer.valueOf(id),
                    resultType,
                    startValue,
                    targetValue,
                    decimals);
            if (kre != null) {
                return Result.ok(KeyResultMapper.toPartShort(kre));
            }
            return Result.fail("KeyResult 不存在");
        }));
    }

    @ApiOperation(
            value = "修改 keyResult 权重",
            notes = "成功后返回受影响的 Objective 进度",
            hidden = true)
    @PutMapping(value = "/{id}/weight/{weight}", produces = "application/json")
    @ResponseBody
    public Result<List<ObjProgress>, String> updateWeight(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "keyResult ID", required = true)
            @PathVariable String id,
            @ApiParam(value = "权重", required = true)
            @PathVariable Double weight) throws Exception {
        return sessionService.protect(sid, (session -> {

            List<ObjProgress> process = keyResultService.updateWeight(Integer.valueOf(id), weight);
            if (process != null) {
                return Result.ok(process);
            }
            return Result.fail("KeyResult 不存在");
        }));

    }

    @ApiOperation(
            value = "修改 keyResult 进度",
            notes = "成功后返回受影响的 Objective 进度")
    @PutMapping(value = "/{id}/progress/{progress}", produces = "application/json")
    @ResponseBody
    public Result<List<ObjProgress>, String> updateProgress(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "keyResult ID", required = true)
            @PathVariable String id,
            @ApiParam(value = "进度(0~100)", required = true)
            @PathVariable Double progress) throws Exception {
        return sessionService.protect(sid, (session -> {
            if (progress < 0 || progress > 100) {
                return Result.fail("进度范围超出[0, 100]");
            }
            List<ObjProgress> process = keyResultService.updateProgress(Integer.valueOf(id), progress);
            if (process != null) {
                return Result.ok(process);
            }
            return Result.fail("KeyResult 不存在");
        }));

    }

    @ApiOperation(
            value = "删除 keyResult",
            notes = "成功后返回受影响的 Objective 进度列表")
    @DeleteMapping(value = "{id}", produces = "application/json")
    @ResponseBody
    public Result<List<ObjProgress>, String> removeKR(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "keyResult ID", required = true)
            @PathVariable String id) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<ObjProgress> process = keyResultService.removeKeyResult(Integer.valueOf(id));
            if (process != null) {
                return Result.ok(process);
            }
            return Result.fail("KeyResult 不存在");
        }));

    }

    @ApiOperation(
            value = "获取 KeyResult ",
            notes = "返回 KeyResult 详细信息列表")
    @GetMapping(value = "{ids}", produces = "application/json")
    @ResponseBody
    public Result<List<KeyResult>, String> getKR(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "KeyResult ID 列表", required = true)
            @PathVariable Integer[] ids) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<KeyResultEntity> results = keyResultService.findAll(Arrays.asList(ids));
            return Result.ok(KeyResultMapper.fromList(results, null));
        }));
    }

    @ApiOperation(
            value = "分配 KeyResult",
            notes = "KeyResult 作为 Objective 分配给 公司/Team/User<br/>" +
                    "返回受到影响的 Objective 列表，其中第一个元素为最新分配的 Objective")
    @PostMapping(value = "/assignment", produces = "application/json")
    @ResponseBody
    public Result<List<ObjProgress>, String> assign(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "KeyResult ID", required = true)
            @RequestParam String kId,
            @ApiParam(value = "Objective 容器类型，1:公司; 2:Team; 3:用户", required = true)
            @RequestParam Integer container,
            @ApiParam(value = "Objective 容器 ID", required = true)
            @RequestParam String cId) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<ObjProgress> process = keyResultService.assignResult(Integer.valueOf(kId), container, Integer.valueOf(cId));
            if (process != null) {
                return Result.ok(process);
            }
            return Result.fail("分配失败");
        }));
    }
}
