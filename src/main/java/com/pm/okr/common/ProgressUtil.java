package com.pm.okr.common;

import com.pm.okr.controller.vo.ObjProgress;
import com.pm.okr.controller.vo.ParamParser;
import com.pm.okr.controller.vo.WeekProgress;
import com.pm.okr.model.entity.ObjectiveEntity;
import com.pm.okr.model.entity.ObjectiveWeekProgressEntity;

import java.util.*;

public class ProgressUtil {

    public final static long ONE_DAY_MS = 1000 * 60 * 60 * 24;

    public static int toDay(long ms) {
        return (int) (ms / ONE_DAY_MS + ((ms % ONE_DAY_MS) > 0 ? 1 : 0));
    }

    public static int numOfWeek(long start, long end) {
        return numOfWeek(toDay(end) - toDay(start) + 1);
    }

    public static int numOfWeek(int dayCount) {
        return dayCount / 7 + (dayCount % 7 > 0 ? 1 : 0);
    }

    public static int nowSeason() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) / 3;
    }

    public static int nowYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }

    public static int nowWeek() {
        int start = toDay(getSeasonStart(nowYear(), nowSeason()));
        int now = toDay(System.currentTimeMillis());
        int nowWeek = numOfWeek(now - start + 1) - 1;

        //第14周并入第13周
        if (nowWeek == 13) {
            nowWeek -= 1;
        }
        return nowWeek;
    }

    public static int week(int year, int season) {
        if (year == nowYear()) {
            if (nowSeason() == season) {
                return nowWeek();
            } else if (nowSeason() > season) {
                return weekCount(year, season) - 1;
            } else {
                return 0;
            }
        } else if (year < nowYear()) {
            return weekCount(year, season) - 1;
        } else {
            return 0;
        }
    }

    //每季度固定13周
    public static int weekCount(int year, int season) {
//        int start = toDay(getSeasonStart(year, season));
//        int end = toDay(getSeasonEnd(year, season));
//        return numOfWeek(end - start + 1);
        return 13;
    }

    public static long getSeasonStart(int year, int season) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, season * 3);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis();
    }

    public static long getSeasonEnd(int year, int season) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, season * 3 + 2);
        Integer maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, maxDay);
        return cal.getTimeInMillis();
    }

    public static Double getExpectedValue(int year, int season, Integer week) {
        if (null == week) {
            week = week(year, season);
        }
        return (week + 1) * 1.0 / weekCount(year, season) * 100;
    }


    public static Integer getStatus(Double progress, Double expected) {
        Integer status;
        if (progress < 0.34 * expected) {
            status = WeekProgress.Status.OFF_TRACK;
        } else if (progress < 0.67 * expected) {
            status = WeekProgress.Status.AT_RISK;
        } else if (progress <= expected) {
            status = WeekProgress.Status.ON_TRACK;
        } else {
            status = WeekProgress.Status.EXCEEDED;
        }
        return status;
    }

    public static ObjProgress computeProgress(ObjectiveEntity obj, Integer week, boolean exact) {
        ObjProgress progress = new ObjProgress();
        progress.setObjectiveId(obj.getId().toString());
        Double expectedValue = getExpectedValue(obj.getYear(), obj.getSeason(), week);
        progress.setExpected(exact ? expectedValue : (Math.round(expectedValue) * 1.0));
        progress.setProgress(exact ? obj.getProgress() : (Math.round(obj.getProgress()) * 1.0));
        progress.setStatus(getStatus(obj.getProgress(), expectedValue));
        return progress;
    }

    public static WeekProgress computeCompositeProgress(List<ObjectiveWeekProgressEntity> objs) {
        if (!objs.isEmpty()) {
            double progress = 0;
            for (ObjectiveWeekProgressEntity owpe : objs) {
                progress += owpe.getProgress();
            }
            progress = progress / objs.size();
            WeekProgress wp = BeanUtil.fill(objs.get(0), new WeekProgress());
            wp.setProgress(Math.round(progress) * 1.0);
            wp.setStatus(ProgressUtil.getStatus(progress, wp.getExpected()));
            wp.setExpected(Math.round(wp.getExpected()) * 1.0);
            wp.setObjId(null);
            return wp;
        }
        return null;
    }

    public static List<WeekProgress> computeCompositeWeekProgress(List<ObjectiveWeekProgressEntity> objs) {
        if (!objs.isEmpty()) {
            Map<String, List<ObjectiveWeekProgressEntity>> wpMap = new HashMap<>();
            for (ObjectiveWeekProgressEntity owpe : objs) {
                String key = String.format("%d%d%02d", owpe.getYear(), owpe.getSeason(), owpe.getWeek());
                if (!wpMap.containsKey(key)) {
                    wpMap.put(key, new ArrayList<>());
                }
                wpMap.get(key).add(owpe);
            }

            List<List<ObjectiveWeekProgressEntity>> wkps = new ArrayList<>(wpMap.values());
            wkps.sort((l1, l2) -> {
                ObjectiveWeekProgressEntity owpe1 = l1.get(0);
                ObjectiveWeekProgressEntity owpe2 = l2.get(0);
                String key1 = String.format("%d%d%02d", owpe1.getYear(), owpe1.getSeason(), owpe1.getWeek());
                String key2 = String.format("%d%d%02d", owpe2.getYear(), owpe2.getSeason(), owpe2.getWeek());
                return key1.compareTo(key2);
            });

            List<WeekProgress> ret = new ArrayList<>();
            for (List<ObjectiveWeekProgressEntity> owps : wkps) {
                ret.add(computeCompositeProgress(owps));
            }
            return ret;
        }
        return Collections.emptyList();
    }

    public static List<ObjProgress> computeProgresses(List<ObjectiveEntity> objs, int week, boolean exact) {
        List<ObjProgress> ret = new ArrayList<>();
        for (ObjectiveEntity obj : objs) {
            ret.add(computeProgress(obj, week, exact));
        }
        return ret;
    }

    public static List<ObjProgress> computeProgresses(List<ObjectiveEntity> objs, boolean exact) {
        List<ObjProgress> ret = new ArrayList<>();
        for (ObjectiveEntity obj : objs) {
            ret.add(computeProgress(obj, null, exact));
        }
        return ret;
    }

    static boolean contains(List<WeekProgress> wps, int year, int season, int week) {
        for (WeekProgress wp : wps) {
            if (wp.getYear() == year && wp.getSeason() == season && wp.getWeek() == week) {
                return true;
            }
        }
        return false;
    }

    static boolean beforeNow(int year, int season, int week) {
        if (year < nowYear()) {
            return true;
        } else if (year == nowYear()) {
            if (season < nowSeason()) {
                return true;
            } else if (season == nowSeason()) {
                if (week <= nowWeek()) {
                    return true;
                }
            }
        }
        return false;
    }

    //补全空缺周进度
    public static List<WeekProgress> completeWeekProgress(List<WeekProgress> wps, ParamParser.YearSeason ys, String objId) {

        List<WeekProgress> ret = new ArrayList<>();
        if (ys.getSeason() == null) {
            //获取整年周进度
            for (int i = 0; i < 4; ++i) {
                int weekCount = weekCount(ys.getYear(), i);
                for (int j = 0; j < weekCount; ++j) {
                    if (beforeNow(ys.getYear(), i, j) && !contains(wps, ys.getYear(), i, j)) {
                        WeekProgress weekProgress = new WeekProgress();
                        weekProgress.setExpected(Math.round(getExpectedValue(ys.getYear(), i, j)) * 1.0);
                        weekProgress.setProgress(0.0);
                        weekProgress.setYear(ys.getYear());
                        weekProgress.setSeason(i);
                        weekProgress.setWeek(j);
                        weekProgress.setStatus(0);
                        weekProgress.setObjId(objId);
                        ret.add(weekProgress);
                    }
                }
            }
        } else {
            //获取当季周进度
            int weekCount = weekCount(ys.getYear(), ys.getSeason());
            for (int j = 0; j < weekCount; ++j) {
                if (beforeNow(ys.getYear(), ys.getSeason(), j) && !contains(wps, ys.getYear(), ys.getSeason(), j)) {
                    WeekProgress weekProgress = new WeekProgress();
                    weekProgress.setExpected(Math.round(getExpectedValue(ys.getYear(), ys.getSeason(), j)) * 1.0);
                    weekProgress.setProgress(0.0);
                    weekProgress.setYear(ys.getYear());
                    weekProgress.setSeason(ys.getSeason());
                    weekProgress.setWeek(j);
                    weekProgress.setStatus(0);
                    weekProgress.setObjId(objId);
                    ret.add(weekProgress);
                }
            }
        }
        ret.addAll(wps);
        ret.sort((l1, l2) -> {
            String key1 = String.format("%d%d%02d", l1.getYear(), l1.getSeason(), l1.getWeek());
            String key2 = String.format("%d%d%02d", l2.getYear(), l2.getSeason(), l2.getWeek());
            return key1.compareTo(key2);
        });
        return ret;
    }
}
