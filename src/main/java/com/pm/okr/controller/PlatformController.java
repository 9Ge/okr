package com.pm.okr.controller;

import com.pm.okr.common.BeanUtil;
import com.pm.okr.common.SessionUtil;
import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;
import com.pm.okr.services.platform.PlatformService;
import com.pm.okr.services.session.SessionService;
import com.pm.okr.services.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@Api(tags = "平台管理")
@Controller
@RequestMapping("/platform")
public class PlatformController {

    @Autowired
    SessionService sessionService;

    @Autowired
    UserService userService;

    @Autowired
    PlatformService platformService;

    @ApiOperation(
            value = "登录",
            notes = "根据用户名密码进行登录, 返回会话ID")
    @PostMapping(value = "/session", produces = "application/json")
    @ResponseBody
    public Result<Session.PartPlatform, String> login(
            @ApiParam(value = "用户名", required = true)
            @RequestParam String email,
            @ApiParam(value = "密码(md5)", required = true)
            @RequestParam String psw) throws InstantiationException, IllegalAccessException, UnsupportedEncodingException {
        SessionService.Session session = sessionService.login(email, psw, 0);
        if (null != session) {
            UserEntity ue = SessionUtil.getUser(session);
            //mu 为0 表示平台管理员用户
            if (ue.getMu() != null && ue.getMu() == 0){
                User.PartShort partShort = new User.PartShort();
                return Result.ok(new Session.PartPlatform(BeanUtil.fill(ue, partShort), session.getId()));
            } else{
                sessionService.logout(session.getId());
                return Result.fail(ErrorCode.USER_PSW_ERROR, "错误");
            }
        }
        return Result.fail(ErrorCode.USER_PSW_ERROR, "错误");
    }

    @ApiOperation(
            value = "登出",
            notes = "登出系统")
    @DeleteMapping(value = "/session", produces = "application/json")
    @ResponseBody
    public Result<String, String> logout(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid) {
        sessionService.logout(sid);
        return Result.ok("用户登出成功");
    }

    @ApiOperation(
            value = "添加管理单元",
            notes = "")
    @PostMapping(value = "mu", produces = "application/json")
    @ResponseBody
    public Result<ManagementUnit, String> addMU(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "管理单元(ManagementUnit.PartAdd)", required = true)
            @RequestBody ManagementUnit.PartAdd mu) throws Exception {
        return sessionService.protect(sid, (session -> {
            try {
                ManagementUnit managementUnit = platformService.addManagementUnit(mu);
                return Result.ok(managementUnit);
            }catch (PlatformService.PlatformException e){
                return Result.fail(e.getMessage());
            }
        }));
    }

    @ApiOperation(
            value = "为管理单元添加管理员",
            notes = "")
    @PostMapping(value = "mu/{mid}/admin", produces = "application/json")
    @ResponseBody
    public Result<User.PartBasic, String> addAdmin(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "管理单元ID", required = true)
            @PathVariable String mid,
            @ApiParam(value = "管理员(User.PartAdmin)", required = true)
            @RequestBody User.PartAdmin admin) throws Exception {
        return sessionService.protect(sid, (session -> {
            try {
                UserEntity ue = platformService.addAdmin(Integer.valueOf(mid), admin);
                return Result.ok(BeanUtil.fill(ue, new User.PartBasic()));
            }catch (PlatformService.PlatformException e){
                return Result.fail(e.getMessage());
            }
        }));
    }

    @ApiOperation(
            value = "为管理单元替换管理员",
            notes = "")
    @PutMapping(value = "mu/{mid}/admin/{oldAdminId}", produces = "application/json")
    @ResponseBody
    public Result<User.PartBasic, String> replaceAdmin(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "管理单元ID", required = true)
            @PathVariable String mid,
            @ApiParam(value = "待替换AdminId", required = true)
            @PathVariable String oldAdminId,
            @ApiParam(value = "新管理员(User.PartAdmin)", required = true)
            @RequestBody User.PartAdmin admin) throws Exception {
        return sessionService.protect(sid, (session -> {
            try {
                UserEntity ue = platformService.replaceAdmin(Integer.valueOf(mid), Integer.valueOf(oldAdminId), admin);
                return Result.ok(BeanUtil.fill(ue, new User.PartBasic()));
            }catch (PlatformService.PlatformException e){
                return Result.fail(e.getMessage());
            }
        }));
    }

    @ApiOperation(
            value = "修改管理单元人数上限",
            notes = "")
    @PutMapping(value = "mu/{mid}/userLimit/{limit}", produces = "application/json")
    @ResponseBody
    public Result<String, String> updateUserLimit(
            @ApiParam(value = "会话ID", required = true)
            @RequestHeader String sid,
            @ApiParam(value = "管理单元ID", required = true)
            @PathVariable String mid,
            @ApiParam(value = "人数上限", required = true)
            @PathVariable Integer limit) throws Exception {
        return sessionService.protect(sid, (session -> {
            if (platformService.updateUserLimit(Integer.valueOf(mid), limit)){
                return Result.ok();
            }
            return Result.fail("修改失败");
        }));
    }


    @ApiOperation(
            value = "修改管理单元人数上限与管理员信息",
            notes = "")
    @PutMapping(value = "mu/{mid}", produces = "application/json")
    @ResponseBody
    public Result<String, String> updateUserLimitOrAdmin(
            @ApiParam(value = "会话ID", required = true)
            @RequestHeader String sid,
            @ApiParam(value = "管理单元ID", required = true)
            @PathVariable String mid,
            @ApiParam(value = "人数上限", required = false)
            @RequestParam(required = false) Integer limit,
            @ApiParam(value = "管理员信息(User.PartEditable)", required = false)
            @RequestBody(required = false) User.PartEditable admin) throws Exception {
        return sessionService.protect(sid, (session -> {
            try {
                UserEntity ue = platformService.updateUserLimitOrAdmin(Integer.valueOf(mid), limit, admin);
                if (ue == null) {
                    return Result.ok();
                }else{
                    return Result.ok(BeanUtil.fill(ue, new User.PartBasic()));
                }
            }catch (PlatformService.PlatformException e){
                return Result.fail(e.getMessage());
            }
        }));
    }

    @ApiOperation(
            value = "删除管理单元",
            notes = "")
    @DeleteMapping(value = "mu/{id}", produces = "application/json")
    @ResponseBody
    public Result<String, String> removeMu(
            @ApiParam(value = "会话ID", required = true)
            @RequestHeader String sid,
            @ApiParam(value = "管理单元ID", required = true)
            @PathVariable String id) throws Exception {
        return sessionService.protect(sid, (session -> {
            platformService.removeMu(Integer.valueOf(id));
            return Result.ok();
        }));

    }

    @ApiOperation(
            value = "获取平台所有管理单元",
            notes = "")
    @GetMapping(value = "mus", produces = "application/json")
    @ResponseBody
    public Result<List<ManagementUnit>, String> getMus(
            @ApiParam(value = "会话ID", required = true)
            @RequestHeader String sid) throws Exception {
        return sessionService.protect(sid, (session -> {
            return Result.ok(platformService.getManagementUnits());
        }));

    }
}
