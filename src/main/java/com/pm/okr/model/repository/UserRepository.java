package com.pm.okr.model.repository;

import com.pm.okr.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    @Query(value = "select u.* from user u left join team_user tu on u.id = tu.user_id where tu.team_id = ?1",
            nativeQuery = true)
    List<UserEntity> findTeamUser(Integer id);

    @Query(value = "select id from user where mu = ?1", nativeQuery = true)
    List<Integer> findIds(Integer mu);

    @Query(value = "select u.id from user u left join team_user tu on u.id = tu.user_id where tu.team_id = ?1", nativeQuery = true)
    List<Integer> findIdsByTeam(Integer id);



    @Query(value = "select u.id from user u left join team_user tu on u.id = tu.user_id where tu.team_id in ?1", nativeQuery = true)
    List<Integer> findIdsByTeams(List<Integer> ids);

    List<UserEntity> findByEmailAndPswAndMu(String name, String psw, Integer mu);

    UserEntity findByIdAndMu(Integer valueOf, Integer currentMu);

    List<UserEntity> findAllByMu(Integer currentMu);

    List<UserEntity> findAllByRoleContainsAndMuAndInit(String admin, Integer id, int i);

    Integer countByMu(Integer id);

    List<UserEntity> findByEmailAndPswAndMuIsNot(String name, String psw, int mu);

    Integer countByMuAndEmail(Integer mid, String email);

    void deleteByMu(Integer muId);

    Integer countByEmailAndMu(String email, Integer currentMu);

    List<UserEntity> findByEmailAndMu(String name, Integer currentMu);
}
