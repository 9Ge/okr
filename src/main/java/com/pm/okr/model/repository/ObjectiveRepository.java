package com.pm.okr.model.repository;

import com.pm.okr.model.entity.CompanyEntity;
import com.pm.okr.model.entity.ObjectiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ObjectiveRepository extends JpaRepository<ObjectiveEntity, Integer> {


    @Query("from ObjectiveEntity where containerType=?1 and containerId=?2 and year = ?3 and mu = ?4")
    List<ObjectiveEntity> findAllByContainerAndMu(Integer container, Integer id, Integer year, Integer mu);


    @Query("from ObjectiveEntity where containerType=?1 and containerId=?2 and year = ?3 and season=?4 and mu = ?5")
    List<ObjectiveEntity> findAllByContainerSeasonAndMu(Integer container, Integer id, Integer year, Integer season, Integer mu);

    ObjectiveEntity findByIdAndMu(Integer id, Integer mu);

    @Query("from ObjectiveEntity where id = ?1")
    ObjectiveEntity findOneById(Integer id);

    List<ObjectiveEntity> findAllByContainerTypeAndContainerIdAndMu(int team, Integer tid, Integer mu);


    @Query("from ObjectiveEntity where " +
            "id <> ?1 and " +
            "(linkAbove is null or linkAbove <> ?1) and " +
            "containerType <= ?2 and " +
            "year = ?3 and " +
            "season=?4 and " +
            "mu = ?5")
    List<ObjectiveEntity> findValidLinkObjectives(Integer id, Integer type, Integer year, Integer season, Integer mu);

    List<ObjectiveEntity> findAllByYearAndSeason(int year, int season);

    List<ObjectiveEntity> findAllByContainerTypeAndContainerIdAndYearAndSeason(Integer containerType, Integer containerId, Integer year, Integer season);

    List<ObjectiveEntity> findAllByIdInAndMu(List<Integer> ids, Integer currentMu);



    @Query("select id from ObjectiveEntity " +
            "where containerType = ?1 and " +
            "containerId in ?2 and " +
            "year=?3 and " +
            "(?4 is null or season = ?4)")
    List<Integer> getObjectiveIds(Integer containerType, List<Integer> ids, Integer year, Integer season);


    @Query("select id from ObjectiveEntity " +
            "where containerType = 1 and " +
            "mu = ?1 and " +
            "year=?2 and " +
            "season = ?3")
    List<Integer> getCompanyObjectiveIds(Integer mu, Integer year, Integer season);


}
