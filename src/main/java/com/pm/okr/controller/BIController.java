package com.pm.okr.controller;

import com.pm.okr.common.ProgressUtil;
import com.pm.okr.controller.vo.*;
import com.pm.okr.services.analysis.BIService;
import com.pm.okr.services.session.SessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Api(tags = "BI 分析")
@Controller
@RequestMapping("/bi")
public class BIController {

    @Autowired
    SessionService sessionService;

    @Autowired
    BIService biService;

    @Getter
    @Setter
    class LatestWeekProcessCompositeCategory {
        @ApiModelProperty(notes = "最近一周 Team 综合周进度")
        WeekContainerProgressMap team;
        @ApiModelProperty(notes = "最近一周 Person 综合周进度")
        WeekContainerProgressMap person;
        @ApiModelProperty(notes = "最近一周 Company 综合周进度")
        WeekContainerProgressMap company;
    }

    @Getter
    @Setter
    class WeekContainerProgressMap {
        @ApiModelProperty(notes = "container ID")
        String key;

        @ApiModelProperty(notes = "周进度")
        WeekProgress value;
    }

    @ApiOperation(
            value = "获取 Objective 进度",
            notes = "取得全年或季度 Objective 周进度" +
                    "如果container 设置为公司，则返回公司、公司下所有Team及Team成员的统计结果<br/>" +
                    "如果container 设置为Team，则返回Team以及Team下所有成员的统计结果<br/>" +
                    "如果container 设置为Person，则返回当前Person的统计结果")
    @GetMapping(value = "progress", produces = "application/json")
    @ResponseBody
    public Result<WeekObjectiveProgressMap, String> getAllProgress(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective 容器类型，1:公司; 2:Team; 3:用户", required = true)
            @RequestParam Integer container,
            @ApiParam(value = "Objective 容器 ID", required = true)
            @RequestParam String id,
            @ApiParam(value = "年份(季度) eg.20191、2019", required = true)
            @RequestParam String yearSeason) throws Exception {
        return sessionService.protect(sid, (session) -> {
            ParamParser.YearSeason ys = ParamParser.getYearSeason(yearSeason);
            List<WeekProgress> weekProgresses = biService.getObjectiveWeekProgress(container, Integer.valueOf(id), ys);

            //key : objectiveId
            //value : 周进度列表
            Map<String, List<WeekProgress>> ret = new HashMap<>();
            //将周进度按照 objective 进行分类
            for (WeekProgress wp : weekProgresses) {
                if (!ret.containsKey(wp.getObjId())) {
                    ret.put(wp.getObjId(), new ArrayList<>());
                }
                ret.get(wp.getObjId()).add(wp);
            }

            //补全各Objective周进度
            for (String objId : ret.keySet()) {
                ret.put(objId, ProgressUtil.completeWeekProgress(ret.get(objId), ys, objId));
                if (ys.getSeason() == null) {
                    standardizingYearWeekProgresses(ret.get(objId));
                }
            }
            return Result.ok(ret);
        });
    }

    /**
     * 标准化全年周进度
     * 全年周目标中，每两周为一个单位
     * eg.
     *   1.某季度有0~12共13周
     *     剔除结果为 1,3,5,7,9,11,12
     *   2.某季度有0~4 共5周
     *     剔除结果为 1,3,4
     * @param wps 已补全待处理全年周进度列表
     * @return 去掉多余周进度后的列表
     */
    List<WeekProgress> standardizingYearWeekProgresses(List<WeekProgress> wps){
        //标记是否保留最后一周
        //对于最后一周为单周的情况，需要保留最后一周
        int tune = 0;
        if (!wps.isEmpty() &&
                wps.get(wps.size() - 1).getWeek() % 2 == 0){
            tune = 1;
        }

        for (int i = wps.size() - 1 - tune; i >= 0; --i) {
            //剔除单周并保留季度末周
            if (wps.get(i).getWeek() % 2 == 0 &&
                    wps.get(i).getWeek() != 12) {
                wps.remove(i);
            }
        }
        return wps;
    }

    @ApiOperation(
            value = "获取 Objective 综合周进度",
            notes = "取得全年或季度 Objective 周综合进度<br/>" +
                    "如果container 设置为公司，则返回公司、公司下所有Team及Team成员的统计结果<br/>" +
                    "如果container 设置为Team，则返回Team以及Team下所有成员的统计结果<br/>" +
                    "如果container 设置为Person，则返回当前Person的统计结果")
    @GetMapping(value = "comprehensiveProgress", produces = "application/json")
    @ResponseBody
    public Result<WeekProcessCompositeCategory, String> getCompositeProgress(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective 容器类型，1:公司; 2:Team; 3:用户", required = true)
            @RequestParam Integer container,
            @ApiParam(value = "Objective 容器 ID", required = true)
            @RequestParam String id,
            @ApiParam(value = "年份(季度) eg.20191、2019", required = true)
            @RequestParam String yearSeason) throws Exception {
        return sessionService.protect(sid, (session) -> {
            ParamParser.YearSeason ys = ParamParser.getYearSeason(yearSeason);
            WeekProcessCompositeCategory wpcc = biService.getCompositeProgress(container, Integer.valueOf(id), ys);
            if (ys.getSeason() == null) {
                standardizingYearWeekProgresses(wpcc.getTeam());
                standardizingYearWeekProgresses(wpcc.getPerson());
                standardizingYearWeekProgresses(wpcc.getCompany());
            }
            return Result.ok(wpcc);
        });
    }

    @ApiOperation(
            value = "获取最近一周 Objective 综合周进度",
            notes = "取得全年或季度 Objective 最近一周综合进度")
    @GetMapping(value = "latestComprehensiveProgress", produces = "application/json")
    @ResponseBody
    public Result<LatestWeekProcessCompositeCategory, String> getLatestProgress(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "年份(季度) eg.20191、2019", required = true)
            @RequestParam String yearSeason) throws Exception {
        return sessionService.protect(sid, (session) -> {
            ParamParser.YearSeason ys = ParamParser.getYearSeason(yearSeason);
            LatestWeekProcessMap lwp = biService.getLatestProgress(ys);
            return Result.ok(lwp);
        });
    }

    @ApiOperation(
            value = "获取 Objective 链接数量",
            notes = "取得全年或季度的链接数量统计")
    @GetMapping(value = "linkedCount", produces = "application/json")
    @ResponseBody
    public Result<LinkedCount, String> getLinkCount(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective 容器类型，1:公司; 2:Team; 3:用户", required = true)
            @RequestParam Integer container,
            @ApiParam(value = "Objective 容器 ID", required = true)
            @RequestParam String id,
            @ApiParam(value = "年份(季度) eg.20191、2019", required = true)
            @RequestParam String yearSeason) throws Exception {
        return sessionService.protect(sid, (session) -> {
            ParamParser.YearSeason ys = ParamParser.getYearSeason(yearSeason);
            LinkedCount linkedCount = biService.getLinkedCount(container, Integer.valueOf(id), ys);
            return Result.ok(linkedCount);
        });
    }

    @ApiOperation(
            value = "获取 Objective 状态统计",
            notes = "取得全年或季度Objective 状态统计")
    @GetMapping(value = "statusCount", produces = "application/json")
    @ResponseBody
    public Result<StatusCount, String> getStatusCount(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective 容器类型，1:公司; 2:Team; 3:用户", required = true)
            @RequestParam Integer container,
            @ApiParam(value = "Objective 容器 ID", required = true)
            @RequestParam String id,
            @ApiParam(value = "年份(季度) eg.20191、2019", required = true)
            @RequestParam String yearSeason) throws Exception {
        return sessionService.protect(sid, (session) -> {
            ParamParser.YearSeason ys = ParamParser.getYearSeason(yearSeason);
            StatusCount sc = biService.getStatusCount(container, Integer.valueOf(id), ys);
            return Result.ok(sc);
        });
    }

    @ApiOperation(
            value = "更新BI统计数据",
            notes = "（供测试用）")
    @PutMapping(value = "count", produces = "application/json")
    @ResponseBody
    public Result<String, String> updateBICollection(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "年份季度 eg.20191", required = true)
            @RequestParam String yearSeason,
            @ApiParam(value = "周", required = true)
            @RequestParam Integer week) throws Exception {
        return sessionService.protect(sid, (session) -> {
            ParamParser.YearSeason ys = ParamParser.getYearSeason(yearSeason);
            biService.collectData(ys.getYear(), ys.getSeason(), week);
            return Result.ok();
        });
    }

    //每晚十一点自动收集统计数据
    @Scheduled(cron = "0 0 23 * * ?")
    public void runDataCollection() {
        int year = ProgressUtil.nowYear();
        int season = ProgressUtil.nowSeason();
        int week = ProgressUtil.nowWeek();
        biService.collectData(year, season, week);
    }

}
