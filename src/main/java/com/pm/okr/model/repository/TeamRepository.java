package com.pm.okr.model.repository;

import com.pm.okr.model.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamRepository extends JpaRepository<TeamEntity, Integer> {

    @Query(
            value = "select * from team t left join team_user tu on t.id = tu.team_id where tu.user_id = ?1",
            nativeQuery = true)
    List<TeamEntity> findByUser(Integer id);

    @Query(
            value = "select count(*) from team_user tu where tu.team_id = ?1",
            nativeQuery = true)
    Integer countMember(Integer tid);

    @Modifying
    @Query(
            value = "delete from team_user where user_id = ?1",
            nativeQuery = true)
    void removeTeamUser(Integer uid);

    @Query(value = "select id from team where mu = ?1", nativeQuery = true)
    List<Integer> findIds(Integer currentMu);

    TeamEntity findByIdAndMu(Integer id, Integer currentMu);

    TeamEntity findByNameAndMu(String name, Integer currentMu);

    List<TeamEntity> findAllByMu(Integer currentMu);

}
