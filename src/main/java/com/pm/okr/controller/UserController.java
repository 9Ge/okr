package com.pm.okr.controller;

import com.pm.okr.common.BeanUtil;
import com.pm.okr.controller.vo.ErrorCode;
import com.pm.okr.controller.vo.Result;
import com.pm.okr.controller.vo.Team;
import com.pm.okr.controller.vo.User;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;
import com.pm.okr.services.session.SessionService;
import com.pm.okr.services.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@CrossOrigin
@Api(tags = "用户管理")
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    SessionService sessionService;

    @Autowired
    UserService userService;

    @ApiOperation(
            value = "注册用户",
            notes = "新建用户，返回新建后的用户对象<br/>" +
                    "用户名不可重复，否则返回错误")
    @PostMapping(value = "", produces = "application/json")
    @ResponseBody
    public Result<User.PartDetail, String> register(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "用户信息(User.PartRegister)", required = true)
            @RequestBody User.PartRegister user) throws Exception {
        return sessionService.protect(sid, (session -> {
            if (user.getName() == null) {
                return Result.fail(ErrorCode.FAILED, "用户名为空");
            }

            if (user.getEmail() == null) {
                return Result.fail(ErrorCode.FAILED, "Email为空");
            }

            if (user.getPsw() == null) {
                return Result.fail(ErrorCode.FAILED, "密码为空");
            }
            UserEntity ue = null;
            synchronized (userService) {
                ue = userService.addUser(user);
            }
            if (ue == null) {
                return Result.fail(ErrorCode.FAILED, "注册用户失败");
            }

            List<TeamEntity> tes = userService.getTeam(Integer.valueOf(ue.getId()));

            User.PartDetail detail = BeanUtil.fill(ue, new User.PartDetail());
            detail.setTeam(BeanUtil.fillList(tes, Team.Part.class));
            return Result.ok(detail);
        }));
    }

    @ApiOperation(
            value = "删除用户",
            notes = "删除用户 后，其对应的OKR 将会一起删除")
    @DeleteMapping(value = "/{uid}", produces = "application/json")
    @ResponseBody
    public Result<String, String> removeUser(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "User Id", required = true)
            @PathVariable String uid) throws Exception {
        return sessionService.protect(sid, (session -> {
            synchronized (userService) {
                userService.removeUser(uid);
            }
            return Result.ok();
        }));
    }

    @ApiOperation(
            value = "更新用户",
            notes = "更新用户信息,返回更新后的用户对象")
    @PutMapping(value = "", produces = "application/json")
    @ResponseBody
    public Result<User.PartDetail, String> modify(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "用户信息(User.PartEditable)", required = true)
            @RequestBody User.PartEditable user) throws Exception {
        return sessionService.protect(sid, (session -> {
            if (user.getId() == null) {
                return Result.fail(ErrorCode.FAILED, "ID 不能为空");
            }
            UserEntity ue = null;
            try {
                synchronized (userService) {
                    ue = userService.updateUser(user);
                }
            } catch (Exception e) {
                return Result.fail(ErrorCode.FAILED, e.getMessage());
            }


            List<TeamEntity> tes = userService.getTeam(Integer.valueOf(ue.getId()));

            User.PartDetail detail = BeanUtil.fill(ue, new User.PartDetail());
            detail.setTeam(BeanUtil.fillList(tes, Team.Part.class));

            return Result.ok(detail);
        }));
    }

    @ApiOperation(
            value = "获取用户列表",
            notes = "")
    @GetMapping(value = "/", produces = "application/json")
    @ResponseBody
    public Result<List<User.PartDetail>, String> getAll(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid) throws Exception {
        return sessionService.protect(sid, (session -> {
            List<UserEntity> users = userService.getUsers();
            List<User.PartDetail> userDetails = BeanUtil.fillList(users, User.PartDetail.class);
            for (User.PartDetail ue : userDetails) {
                List<TeamEntity> tes = userService.getTeam(Integer.valueOf(ue.getId()));
                ue.setTeam(BeanUtil.fillList(tes, Team.Part.class));
            }
            return Result.ok(userDetails);
        }));

    }
}
