package com.pm.okr.controller;

import com.pm.okr.common.BeanUtil;
import com.pm.okr.common.SessionUtil;
import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;
import com.pm.okr.services.session.SessionService;
import com.pm.okr.services.user.UserService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@CrossOrigin
@Api(tags = "会话管理")
@Controller
@RequestMapping("/session")
public class SessionController {


    @Autowired
    SessionService sessionService;


    @Autowired
    UserService userService;

    @ApiOperation(
            value = "登录",
            notes = "根据用户名密码进行登录, 返回会话ID")
    @PostMapping(value = "/", produces = "application/json")
    @ResponseBody
    public Result<Session, String> login(
            @ApiParam(value = "邮箱", required = true)
            @RequestParam String email,
            @ApiParam(value = "密码(md5)", required = true)
            @RequestParam String psw,
            @ApiParam(value = "管理单元ID")
            @RequestParam(required = false) Integer mu) throws InstantiationException, IllegalAccessException, UnsupportedEncodingException {
        SessionService.Session session = sessionService.login(email, psw, mu);
        if (null != session) {
            UserEntity ue = SessionUtil.getUser(session);
            User.PartDetail detail = BeanUtil.fill(ue, new User.PartDetail());
            List<TeamEntity> tes = userService.getTeam(ue.getId());
            detail.setTeam(BeanUtil.fillList(tes, Team.Part.class));
            return Result.ok(new Session(detail, session.getId()));
        }
        return Result.fail(ErrorCode.USER_PSW_ERROR, "错误");
    }

    @ApiOperation(
            value = "登出",
            notes = "登出系统")
    @DeleteMapping(value = "/", produces = "application/json")
    @ResponseBody
    public Result<String, String> logout(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid) {
        sessionService.logout(sid);
        return Result.ok("用户登出成功");
    }

    @ApiOperation(
            value = "当前用户",
            notes = "返回当前用户Session")
    @GetMapping(value = "/user", produces = "application/json")
    @ResponseBody
    public Result<Session, String> currentUser(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid) throws Exception {
        return sessionService.protect(sid, (session -> {
            UserEntity ue = userService.getUser(SessionUtil.currentUser().getId());
            SessionUtil.setUser(SessionUtil.currentSession(), ue);
            User.PartDetail detail = BeanUtil.fill(ue, new User.PartDetail());
            List<TeamEntity> tes = userService.getTeam(ue.getId());
            detail.setTeam(BeanUtil.fillList(tes, Team.Part.class));
            return Result.ok(new Session(detail, session.getId()));
        }));
    }
}
