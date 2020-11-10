package com.pm.okr.services.user;

import com.pm.okr.controller.vo.User;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;

import java.io.UnsupportedEncodingException;
import java.util.List;


public interface UserService {
    UserEntity addUser(User.PartRegister user) throws UnsupportedEncodingException;

    void removeUser(String uid);

    UserEntity updateUser(User.PartEditable user) throws Exception;

    List<TeamEntity> getTeam(Integer uId);

    UserEntity getUser(Integer uId);

    List<UserEntity> getUsers();
}
