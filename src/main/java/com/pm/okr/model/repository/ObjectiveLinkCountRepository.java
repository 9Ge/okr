package com.pm.okr.model.repository;

import com.pm.okr.model.entity.ObjectiveLinkCountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ObjectiveLinkCountRepository extends JpaRepository<ObjectiveLinkCountEntity, Integer> {

    ObjectiveLinkCountEntity findByYearAndSeasonAndContainerTypeAndContainerId(int year, int season, Integer type, Integer id);

    @Query(
            value = "select sum(olc.linkup_count), sum(olc.not_linkup_count) from objective_link_count olc " +
                    "where olc.container_type = 3 and olc.year = ?2 and (olc.season = ?3 or ?3 is null) and olc.container_id in (" +
                    "select user_id from team_user where team_id = ?1)",
            nativeQuery = true)
    List<Integer[]> sumAllUserObjectiveLinkupCount(Integer teamId, Integer year, Integer season);

    @Query(
            value = "select sum(olc.linkup_count), sum(olc.not_linkup_count) from objective_link_count olc " +
                    "where olc.container_type = 3 and olc.year = ?2 and (olc.season = ?3 or ?3 is null) and olc.container_id in (" +
                    "select user_id from team_user where user_id = ?1)",
            nativeQuery = true)
    List<Integer[]> sumOneUserObjectiveLinkupCount(Integer userId, Integer year, Integer season);

    @Query(
            value = "select sum(olc.linkup_count), sum(olc.not_linkup_count) from objective_link_count olc " +
                    "where olc.mu = ?3 and olc.container_type = 2 and olc.year = ?1 and (olc.season = ?2 or ?2 is null) and olc.container_id in" +
                    "(select id from team)",
            nativeQuery = true)
    List<Integer[]> sumAllTeamObjectiveLinkupCount(Integer year, Integer season, Integer mu);

    @Query(
            value = "select sum(olc.linkup_count), sum(olc.not_linkup_count) from objective_link_count olc " +
                    "where olc.container_type = 2 and container_id = ?1 and olc.year = ?2 and (olc.season = ?3 or ?3 is null) and olc.container_id in" +
                    "(select id from team)",
            nativeQuery = true)
    List<Integer[]> sumOneTeamObjectiveLinkupCount(Integer teamId, Integer year, Integer season);

    @Query(
            value = "select sum(olc.linkup_count), sum(olc.not_linkup_count) from objective_link_count olc " +
                    "where olc.mu = ?3 and olc.container_type = 3 and olc.year = ?1 and  (olc.season = ?2 or ?2 is null) and olc.container_id in" +
                    "(select user_id from team_user)",
            nativeQuery = true)
    List<Integer[]> sumAllTeamUserObjectiveLinkupCount(Integer year, Integer season, Integer mu);
}
