package com.pm.okr.services.util;

import com.pm.okr.common.ObjectiveMapper;
import com.pm.okr.model.entity.CompanyEntity;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.model.entity.TeamEntity;
import com.pm.okr.model.entity.UserEntity;
import com.pm.okr.model.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class EntityLoaderImpl implements ObjectiveMapper.EntityLoader {


    @Autowired
    UserRepository userRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    ObjectiveRepository objectiveRepository;


    @Override
    public CompanyEntity getCompany(Integer cid) {
        Optional<CompanyEntity> ret = companyRepository.findById(cid);
        if (ret.isPresent()){
            return ret.get();
        }
        return null;
    }

    @Override
    public TeamEntity getTeam(Integer tid) {
        Optional<TeamEntity> ret = teamRepository.findById(tid);
        if (ret.isPresent()){
            return ret.get();
        }
        return null;
    }

    @Override
    public UserEntity getUser(Integer uid) {
        Optional<UserEntity> ret = userRepository.findById(uid);
        if (ret.isPresent()){
            return ret.get();
        }
        return null;
    }

    @Override
    public ObjectiveEntity getObjective(Integer oId) {
        return objectiveRepository.findOneById(oId);
    }
}
