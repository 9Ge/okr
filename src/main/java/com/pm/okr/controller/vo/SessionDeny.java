package com.pm.okr.controller.vo;

import com.pm.okr.services.session.SessionService;

public class SessionDeny implements SessionService.OnSessionDeny {
    @Override
    public Result onSession(String sid) {
        return Result.fail(ErrorCode.NOT_LOGIN, "用户未登录");
    }

    static SessionDeny deny = new SessionDeny();

    public static SessionDeny deny(){
        return deny;
    }
}
