package com.pm.okr.services.session;

import com.pm.okr.controller.vo.Result;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface SessionService {



    interface Session{
        String getId();
        Map<String, Object> getAttrs();
    }

    interface OnSessionPass{
        Result onSession(Session session) throws Exception;
    }

    interface OnSessionDeny{
        Result onSession(String sid);
    }

    Session login(String name, String psw, Integer mu) throws UnsupportedEncodingException;

    boolean logout(String sid);

    <T> Result<T, String> protect(String sid, OnSessionDeny onSessionDeny, OnSessionPass onSession) throws Exception;

    <T> Result<T, String> protect(String sid, OnSessionPass onSession) throws Exception;

    Session createSession();
}
