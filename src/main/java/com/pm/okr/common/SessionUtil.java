package com.pm.okr.common;

import com.pm.okr.model.entity.UserEntity;
import com.pm.okr.services.session.SessionService;

public class SessionUtil {
    public final static String USER_KEY = "_ue_";
    public final static String MU_KEY = "_mu_";

    static ThreadLocal<SessionService.Session> threadSession = new ThreadLocal<>();

    public static SessionService.Session setUser(SessionService.Session session, UserEntity user){
        session.getAttrs().put(USER_KEY, user);
        session.getAttrs().put(MU_KEY, user.getMu());
        return session;
    }

    public static void setSession(SessionService.Session session){
        threadSession.set(session);
    }


    public static void removeSession(){
        threadSession.remove();
    }

    public static SessionService.Session currentSession(){
        return threadSession.get();
    }

    public static UserEntity currentUser(){
        return getUser(currentSession());
    }

    public static Integer currentMu(){
        return getMu(currentSession());
    }

    public static UserEntity getUser(SessionService.Session session){
        return (UserEntity) session.getAttrs().get(USER_KEY);
    }

    public static Integer getMu(SessionService.Session session){
        return (Integer) session.getAttrs().get(MU_KEY);
    }
}
