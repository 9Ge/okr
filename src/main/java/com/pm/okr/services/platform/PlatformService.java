package com.pm.okr.services.platform;

import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.UserEntity;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface PlatformService {

    UserEntity replaceAdmin(Integer mid, Integer oldAdminId, User.PartAdmin admin) throws UnsupportedEncodingException, PlatformException;

    UserEntity updateUserLimitOrReplaceAdmin(Integer valueOf, Integer limit, Integer oldAdminId, User.PartAdmin admin) throws PlatformException, UnsupportedEncodingException;

    UserEntity updateUserLimitOrAdmin(Integer valueOf, Integer limit, User.PartEditable admin) throws PlatformException, UnsupportedEncodingException;

    void removeMu(Integer muId);

    public class PlatformException extends Exception{
        public PlatformException(String message) {
            super(message);
        }
    }

    UserEntity addAdmin(Integer valueOf, User.PartAdmin admin) throws PlatformException, UnsupportedEncodingException;

    boolean updateUserLimit(Integer valueOf, Integer limit);

    List<ManagementUnit> getManagementUnits();

    ManagementUnit addManagementUnit(ManagementUnit.PartAdd mu) throws UnsupportedEncodingException, PlatformException;
}
