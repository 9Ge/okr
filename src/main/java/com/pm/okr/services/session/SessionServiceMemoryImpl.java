package com.pm.okr.services.session;

import com.pm.okr.common.SessionUtil;
import com.pm.okr.controller.vo.Result;
import com.pm.okr.controller.vo.SessionDeny;
import com.pm.okr.model.entity.UserEntity;
import com.pm.okr.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
public class SessionServiceMemoryImpl implements SessionService {

    class MemorySession implements Session {

        String id;
        Map<String, Object> attrs;

        public MemorySession(String id, Map<String, Object> attrs) {
            this.id = id;
            this.attrs = attrs;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Map<String, Object> getAttrs() {
            return attrs;
        }
    }

    @Autowired
    UserRepository userRepository;

    Map<String, Map<String, Object>> sessions = Collections.synchronizedMap(new HashMap<>());

    @Autowired
    public void init() {
        sessions.put("test", Collections.synchronizedMap(new HashMap<>()));
    }

    @Override
    public Session login(String name, String psw, Integer mu) throws UnsupportedEncodingException {
        List<UserEntity> ues = null;
        if (mu != null) {
            ues = userRepository.findByEmailAndPswAndMu(name, psw, mu);
        } else {
            ues = userRepository.findByEmailAndPswAndMuIsNot(name, psw, 0);
        }

        if (!ues.isEmpty()) {
            Session session = createSession();
            SessionUtil.setUser(session, ues.get(0));
            return session;
        }
        return null;
    }

    @Override
    public boolean logout(String sid) {
        boolean ret = sessions.containsKey(sid);
        sessions.remove(sid);
        return ret;
    }

    @Override
    public Result protect(String sid, SessionService.OnSessionDeny onSessionDeny, SessionService.OnSessionPass onSession) throws Exception {
        Map attrs = sessions.get(sid);
        if (null != attrs) {
            try {
                Session session = new MemorySession(sid, attrs);
                //保存当前请求会话
                SessionUtil.setSession(session);
                return onSession.onSession(session);
            } finally {
                //清理当前请求会话
                SessionUtil.removeSession();
            }
        }
        return onSessionDeny.onSession(sid);
    }

    @Override
    public Result protect(String sid, OnSessionPass onSession) throws Exception {
        return protect(sid, SessionDeny.deny(), onSession);
    }

    @Override
    public Session createSession() {
        String sid = System.currentTimeMillis() + "" + new Random().nextInt(1000);
        sessions.put(sid, Collections.synchronizedMap(new HashMap<>()));
        return new MemorySession(sid, sessions.get(sid));
    }
}
