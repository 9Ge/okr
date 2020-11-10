package com.pm.okr.services.platform;

import com.pm.okr.common.BeanUtil;
import com.pm.okr.common.SessionUtil;
import com.pm.okr.controller.vo.*;
import com.pm.okr.model.entity.*;
import com.pm.okr.model.entity.ManagementUnitEntity;
import com.pm.okr.model.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class PlatformServiceImpl implements PlatformService {


    @Autowired
    UserRepository userRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    ManagementUnitRepository managementUnitRepository;

    @Override
    public UserEntity replaceAdmin(Integer mid, Integer oldAdminId, User.PartAdmin admin) throws UnsupportedEncodingException, PlatformException {
        UserEntity ret = addAdmin(mid, admin);
        UserEntity oldAdmin = userRepository.findByIdAndMu(oldAdminId, mid);
        if (oldAdmin != null){
            oldAdmin.setInit(0);
            userRepository.save(oldAdmin);
        }
        return ret;
    }

    UserEntity updateAdmin(Integer mid, User.PartEditable admin) throws UnsupportedEncodingException, PlatformException {

        UserEntity oldAdmin = userRepository.findByIdAndMu(Integer.valueOf(admin.getId()), mid);
        if (oldAdmin == null){
            throw new PlatformException("用户不存在");
        }

        if (!(admin.getName() == null || admin.getName().isEmpty())) {
            oldAdmin.setName(admin.getName());
        }

        if (!(admin.getPsw() == null || admin.getPsw().isEmpty())) {
            oldAdmin.setPsw(DigestUtils.md5DigestAsHex(admin.getPsw().getBytes("utf-8")));
        }

        if (!(admin.getEmail() == null || admin.getEmail().isEmpty())) {
            if (!admin.getEmail().equals(oldAdmin.getEmail())){
                if (userRepository.countByMuAndEmail(mid, admin.getEmail()) > 0) {
                    throw new PlatformException("用户 " + admin.getEmail() + " 邮箱已存在");
                }
            }
            oldAdmin.setEmail(admin.getEmail());
        }

        oldAdmin.setColor(admin.getColor());
        userRepository.save(oldAdmin);
        return oldAdmin;
    }

    @Override
    public UserEntity updateUserLimitOrReplaceAdmin(Integer mid, Integer limit, Integer oldAdminId, User.PartAdmin admin) throws PlatformException, UnsupportedEncodingException {
        if (limit != null && !updateUserLimit(mid, limit)){
            throw new PlatformException("修改用户上限失败");
        }
        if (oldAdminId != null && admin != null){
            return replaceAdmin(mid, oldAdminId, admin);
        }
        return null;
    }

    @Override
    public UserEntity updateUserLimitOrAdmin(Integer mid, Integer limit, User.PartEditable admin) throws PlatformException, UnsupportedEncodingException {
        if (limit != null && !updateUserLimit(mid, limit)){
            throw new PlatformException("修改用户上限失败");
        }
        if (admin != null && admin.getId() != null){
            return updateAdmin(mid, admin);
        }
        return null;
    }

    @Override
    public void removeMu(Integer muId) {
        Optional<ManagementUnitEntity> mue = managementUnitRepository.findById(muId);
        if (mue.isPresent()){
            managementUnitRepository.deleteById(muId);
            userRepository.deleteByMu(muId);
        }
    }

    @Override
    public UserEntity addAdmin(Integer mid, User.PartAdmin admin) throws PlatformException, UnsupportedEncodingException {

        if (admin.getName() == null || admin.getEmail().isEmpty()) {
            throw new PlatformException("邮箱不能为");
        }

        if (admin.getPsw() == null || admin.getPsw().isEmpty()) {
            throw new PlatformException("密码不能为空");
        }

        if (userRepository.countByMuAndEmail(mid, admin.getEmail()) > 0) {
            throw new PlatformException("用户 " + admin.getEmail() + " 已存在");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setMu(mid);
        userEntity.setColor(admin.getColor());
        userEntity.setEmail(admin.getEmail());
        userEntity.setName(admin.getName());
        userEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        userEntity.setRole("ADMIN");
        userEntity.setInit(1);
        userEntity.setPsw(DigestUtils.md5DigestAsHex(admin.getPsw().getBytes("utf-8")));
        userEntity = userRepository.saveAndFlush(userEntity);
        return userEntity;
    }

    @Override
    public boolean updateUserLimit(Integer mid, Integer limit) {
        if (mid == null || limit == null || limit <= 0) {
            return false;
        }
        Optional<ManagementUnitEntity> managementUnitEntity = managementUnitRepository.findById(mid);
        if (managementUnitEntity.isPresent()) {
            managementUnitEntity.get().setUserLimit(limit);
            managementUnitEntity.get().setUpdateUser(SessionUtil.currentUser().getId());
            managementUnitEntity.get().setUpdateTime(new Timestamp(System.currentTimeMillis()));
            managementUnitRepository.saveAndFlush(managementUnitEntity.get());
            return true;
        }
        return false;
    }

    ManagementUnit mue2mu(ManagementUnitEntity mue){
        ManagementUnit managementU = new ManagementUnit();
        Company company = new Company();
        company.setTeams(Collections.emptyList());
        managementU.setCompany(
                BeanUtil.fill(companyRepository.findAllByMu(mue.getId()).get(0),
                company));
        managementU.setId(mue.getId().toString());
        managementU.setUserLimit(mue.getUserLimit());
        managementU.setUserCount(userRepository.countByMu(mue.getId()));
        List<User.PartBasic> details = new ArrayList<>();
        for (UserEntity ue:userRepository.findAllByRoleContainsAndMuAndInit("ADMIN", mue.getId(), 1)) {
            details.add(BeanUtil.fill(ue, new User.PartBasic()));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        managementU.setAdmins(details);
        managementU.setCreateTime(sdf.format(mue.getCreateTime()));
        managementU.setUpdateTime(sdf.format(mue.getUpdateTime()));

        User.PartShort createUser = new User.PartShort();
        BeanUtil.fill(userRepository.findById(mue.getCreateUser()).get(), createUser);
        managementU.setCreateUser(createUser);

        User.PartShort updateUser = new User.PartShort();
        BeanUtil.fill(userRepository.findById(mue.getUpdateUser()).get(), updateUser);
        managementU.setUpdateUser(createUser);
        return managementU;
    }

    @Override
    public List<ManagementUnit> getManagementUnits() {
        List<ManagementUnit> mus = new ArrayList<>();
        for (ManagementUnitEntity mue : managementUnitRepository.findAll()) {
            mus.add(mue2mu(mue));
        }
        return mus;
    }

    @Override
    public ManagementUnit addManagementUnit(ManagementUnit.PartAdd mu) throws UnsupportedEncodingException, PlatformException {

        if (mu.getUserLimit() == null || mu.getUserLimit() <= 0) {
            throw new PlatformException("用户上限错误");
        }

        if (mu.getCompanyName() == null || mu.getCompanyName().isEmpty()) {
            throw new PlatformException("公司名称不能为空");
        }

        ManagementUnitEntity managementUnit = new ManagementUnitEntity();
        managementUnit.setCreateTime(new Timestamp(System.currentTimeMillis()));
        managementUnit.setCreateUser(SessionUtil.currentUser().getId());
        managementUnit.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        managementUnit.setUpdateUser(SessionUtil.currentUser().getId());
        managementUnit.setUserLimit(mu.getUserLimit());
        managementUnit = managementUnitRepository.saveAndFlush(managementUnit);

        CompanyEntity companyEntity = new CompanyEntity();
        companyEntity.setName(mu.getCompanyName());
        companyEntity.setMu(managementUnit.getId());
        companyRepository.saveAndFlush(companyEntity);

        if (mu.getAdmins() != null && !mu.getAdmins().isEmpty()) {
            Set<String> usedAdmin = new HashSet<>();
            for (User.PartAdmin admin : mu.getAdmins()) {
                if (!usedAdmin.contains(admin.getName())) {
                   addAdmin(managementUnit.getId(), admin);
                }
                usedAdmin.add(admin.getName());
            }
        }

        return mue2mu(managementUnit);
    }
}
