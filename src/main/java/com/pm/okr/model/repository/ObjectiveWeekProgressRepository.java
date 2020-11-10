package com.pm.okr.model.repository;

import com.pm.okr.model.entity.ObjectiveWeekProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ObjectiveWeekProgressRepository extends JpaRepository<ObjectiveWeekProgressEntity, Integer> {

    ObjectiveWeekProgressEntity findByYearAndSeasonAndWeekAndObjId(int year, int season, int week, Integer objId);

    @Query(
            value = "from ObjectiveWeekProgressEntity owpe where owpe.mu = ?3 and owpe.objId in (select id from ObjectiveEntity oe " +
                    "where oe.containerType = ?1 and oe.year = ?2 and oe.mu = ?3)"
    )
    List<ObjectiveWeekProgressEntity> findAllContainerYearWeekProgress(Integer container, Integer year, Integer mu);

    @Query(
            value = "from ObjectiveWeekProgressEntity owpe where owpe.mu = ?4 and owpe.objId in (select id from ObjectiveEntity oe " +
                    "where oe.containerType = ?1 and oe.year = ?2 and oe.season = ?3 and oe.mu = ?4)"
    )
    List<ObjectiveWeekProgressEntity> findAllContainerYearSeasonWeekProgress(Integer container, Integer year, Integer season, Integer mu);

    @Query(
            value = "from ObjectiveWeekProgressEntity owpe where owpe.objId in (select id from ObjectiveEntity oe " +
                    "where oe.containerType = ?1 and oe.containerId = ?2 and oe.year = ?3 and oe.mu = ?4)"
    )
    List<ObjectiveWeekProgressEntity> findAllContainerIdYearWeekProgress(Integer container, Integer id, Integer year, Integer mu);

    @Query(
            value = "from ObjectiveWeekProgressEntity owpe where owpe.objId in (select id from ObjectiveEntity oe " +
                    "where oe.containerType = ?1 and oe.containerId = ?2 and oe.year = ?3 and oe.season = ?4)"
    )
    List<ObjectiveWeekProgressEntity> findByYearSeasonWeekProgress(Integer container, Integer id, Integer year, Integer season);

    @Query(value = "from ObjectiveWeekProgressEntity owpe where owpe.year = ?1 and owpe.mu = ?2")
    List<ObjectiveWeekProgressEntity> findAllYearWeekProgress(Integer year, Integer mu);

    @Query(value = "from ObjectiveWeekProgressEntity owpe where owpe.year = ?1 and owpe.season = ?2 and owpe.mu = ?2")
    List<ObjectiveWeekProgressEntity> findAllYearSeasonWeekProgress(Integer year, Integer season, Integer mu);


    @Query(
            value = "select * from objective_week_progress owpe " +
                    "where owpe.obj_id in " +
                    "   (" +
                    "       select objs.id from objectives objs " +
                    "       left join team_user tu on objs.container_id = tu.user_id " +
                    "       where " +
                    "           tu.team_id = ?1 and " +
                    "           objs.container_type = 3 and " +
                    "           objs.year = ?2" +
                    "   )",
            nativeQuery = true
    )
    List<ObjectiveWeekProgressEntity> findTeamUserWeekProgress(Integer teamId, Integer year);

    @Query(
            value = "select * from objective_week_progress owpe " +
                    "where owpe.obj_id in " +
                    "   (" +
                    "       select objs.id from objectives objs " +
                    "       left join team_user tu on objs.container_id = tu.user_id " +
                    "       where " +
                    "           tu.team_id = ?1 and " +
                    "           objs.container_type = 3 and " +
                    "           objs.year = ?2 and " +
                    "           objs.season = ?3" +
                    "   )",
            nativeQuery = true
    )
    List<ObjectiveWeekProgressEntity> findTeamUserWeekProgress(Integer teamId, Integer year, Integer season);



    @Query(
            value = "select " +
                    "   sum(case when owpe.status = 0 then 1 else 0 end) as offTrackCount, " +
                    "   sum(case when owpe.status = 1 then 1 else 0 end) as atRiskCount, " +
                    "   sum(case when owpe.status = 2 then 1 else 0 end) as onTrackCount, " +
                    "   sum(case when owpe.status = 3 then 1 else 0 end) as exceededCount " +
                    "from objective_week_progress owpe " +
                    "where " +
                    "owpe.obj_Id in ?1 and " +
                    "owpe.week = (" +
                    "   select max(owpe2.week) " +
                    "   from objective_week_progress owpe2 " +
                    "   where owpe2.obj_Id in ?1)",
            nativeQuery = true
    )
    List<Integer[]> getSeasonObjectiveStatusCount(List<Integer> seasonObjIds);

    @Query(
            value = "from ObjectiveWeekProgressEntity owpe where owpe.objId in (select id from ObjectiveEntity oe " +
                    "where oe.containerType = 2 and oe.containerId = ?1 and oe.year = ?2)"
    )
    List<ObjectiveWeekProgressEntity> findAllTeamYearWeekProgress(Integer teamId, Integer year);

    @Query(
            value = "from ObjectiveWeekProgressEntity owpe where owpe.objId in (select id from ObjectiveEntity oe " +
                    "where oe.containerType = 2 and oe.containerId = ?1 and oe.year = ?2 and oe.season = ?3)"
    )
    List<ObjectiveWeekProgressEntity> findAllTeamYearSeasonWeekProgress(Integer teamId, Integer year, Integer season);

    @Query(
            value = "select * from objective_week_progress owpe where owpe.obj_Id in (" +
                    "       select objs.id from objectives objs " +
                    "       left join team_user tu on objs.container_id = tu.user_id " +
                    "       where " +
                    "           tu.team_id = ?1 and " +
                    "           objs.container_type = 3 and " +
                    "           objs.year = ?2" +
                    ")",
            nativeQuery = true
    )
    List<ObjectiveWeekProgressEntity> findAllTeamUserYearWeekProgress(Integer teamId, Integer year);

    @Query(
            value = "select * from objective_week_progress owpe where owpe.obj_Id in (" +
                    "       select objs.id from objectives objs " +
                    "       left join team_user tu on objs.container_id = tu.user_id " +
                    "       where " +
                    "           tu.team_id = ?1 and " +
                    "           objs.container_type = 3 and " +
                    "           objs.year = ?2 and " +
                    "           objs.season = ?3" +
                    ")",
            nativeQuery = true
    )
    List<ObjectiveWeekProgressEntity> findAllTeamUserYearSeasonWeekProgress(Integer teamId, Integer year, Integer season);

    @Query(
            value = "from ObjectiveWeekProgressEntity owpe where owpe.objId in (select id from ObjectiveEntity oe " +
                    "where oe.containerType = 3 and oe.containerId = ?1 and oe.year = ?2)"
    )
    List<ObjectiveWeekProgressEntity> findAllUserYearWeekProgress(Integer uId, Integer year);


    @Query(
            value = "from ObjectiveWeekProgressEntity owpe where owpe.objId in (select id from ObjectiveEntity oe " +
                    "where oe.containerType = 3 and oe.containerId = ?1 and oe.year = ?2 and oe.season = ?3)"
    )
    List<ObjectiveWeekProgressEntity> findAllUserYearSeasonWeekProgress(Integer uId, Integer year, Integer season);

    @Query(
            value = "select owpe.* from objective_week_progress owpe " +
                    "where " +
                    "owpe.mu = ?3 and " +
                    "owpe.obj_Id in " +
                    "(select id from objectives oe where oe.year = ?1 and oe.season = ?2) " +
                    "and owpe.week = " +
                    "(select max(owpe2.week) from objective_week_progress owpe2 where owpe2.obj_Id in (select id from objectives oe " +
                    " where oe.year = ?1 and oe.season = ?2))",
            nativeQuery = true
    )
    List<ObjectiveWeekProgressEntity> findAllLatestYearSeasonWeekProgress(Integer year, Integer season, Integer mu);

    @Query(
            value = "select owpe.* from objective_week_progress owpe where " +
                    "owpe.mu = ?2 and " +
                    "owpe.obj_Id in (select id from objectives oe " +
                    "where oe.year = ?1) and (owpe.season * 100 + owpe.week) = " +
                    "(select max(owpe2.season * 100 + owpe2.week) from objective_week_progress owpe2 where owpe2.obj_Id in (select id from objectives oe " +
                    "                    where oe.year = ?1))",
            nativeQuery = true
    )
    List<ObjectiveWeekProgressEntity> findAllLatestYearWeekProgress(Integer year, Integer mu);

}
