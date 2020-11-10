package com.pm.okr.services.user;

import com.pm.okr.common.BeanUtil;
import com.pm.okr.common.SessionUtil;
import com.pm.okr.controller.vo.ObjectiveContainer;
import com.pm.okr.controller.vo.User;
import com.pm.okr.model.entity.ManagementUnitEntity;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;
import com.pm.okr.model.repository.*;
import com.pm.okr.services.objective.ObjectiveService;
import com.pm.okr.services.objective.ObjectiveServiceImpl;
import com.pm.okr.services.util.ProgressUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;


    @Autowired
    TeamRepository teamRepository;

    @Autowired
    ObjectiveRepository objectiveRepository;

    @Autowired
    KeyResultRepository keyResultRepository;

    @Autowired
    ObjectiveService objectiveService;

    @Autowired
    ManagementUnitRepository managementUnitRepository;

    @Override
    public UserEntity addUser(User.PartRegister user) throws UnsupportedEncodingException {
        if (userRepository.countByEmailAndMu(user.getEmail(), SessionUtil.currentMu()) > 0) {
            return null;
        }

        ManagementUnitEntity mue = managementUnitRepository.findById(SessionUtil.currentMu()).get();
        if (mue.getUserLimit() <= userRepository.countByMu(mue.getId())) {
            return null;
        }

        UserEntity ue = new UserEntity();
        BeanUtil.fill(user, ue);
        ue.setMu(SessionUtil.currentMu());
        ue.setCreateTime(new Timestamp(System.currentTimeMillis()));
        ue.setPsw(DigestUtils.md5DigestAsHex(user.getPsw().getBytes("utf-8")));
        ue = userRepository.saveAndFlush(ue);
//        Optional<TeamEntity> defaultTeam = teamRepository.findById(1);
//        if (defaultTeam.isPresent()) {
//            defaultTeam.get().getMembers().add(ue);
//            teamRepository.saveAndFlush(defaultTeam.get());
//        }
        return ue;
    }

    @Override
    public void removeUser(String uid) {
        UserEntity userEntity = userRepository.findByIdAndMu(Integer.valueOf(uid), SessionUtil.currentMu());
        if (null != userEntity) {
            userRepository.deleteById(userEntity.getId());
            List<ObjectiveEntity> oes = objectiveRepository.findAllByContainerTypeAndContainerIdAndMu(ObjectiveContainer.USER, userEntity.getId(), userEntity.getMu());

            for (ObjectiveEntity oe : oes) {
                objectiveService.removeObjective(oe.getId());
            }
        }
    }

    @Override
    public UserEntity updateUser(User.PartEditable user) throws Exception {
        Integer uid = Integer.valueOf(user.getId());
        UserEntity oldUe = userRepository.findByIdAndMu(uid, SessionUtil.currentMu());

        if (oldUe == null) {
            throw new Exception("用户不存在");
        }

        String psw = user.getPsw();
        if (psw != null && !psw.isEmpty()) {

            if (null == user.getOldPsw() || !oldUe.getPsw().equals(DigestUtils.md5DigestAsHex(user.getOldPsw().getBytes("utf-8")))) {
                throw new Exception("原始密码校验失败");
            }

            user.setPsw(DigestUtils.md5DigestAsHex(psw.getBytes("utf-8")));
        }


        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            List<UserEntity> us = userRepository.findByEmailAndMu(user.getEmail(), SessionUtil.currentMu());
            if (us.size() == 1) {
                if (!uid.equals(us.get(0).getId())) {
                    throw new Exception("Email 已注册");
                }
            } else if (us.size() > 1){
                throw new Exception("Email 已注册");
            }
        }

        BeanUtil.fill(user, oldUe);

        return userRepository.saveAndFlush(oldUe);
    }


    @Override
    public List<TeamEntity> getTeam(Integer uId) {
        return teamRepository.findByUser(uId);
    }

    @Override
    public UserEntity getUser(Integer uId) {
        return userRepository.findById(uId).get();
    }

    @Override
    public List<UserEntity> getUsers() {
        return userRepository.findAllByMu(SessionUtil.currentMu());
    }
}
