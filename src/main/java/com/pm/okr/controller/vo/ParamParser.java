package com.pm.okr.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ParamParser {

    @AllArgsConstructor
    @Getter
    public static class YearSeason{
        Integer year;
        Integer season;
    }

    public static YearSeason getYearSeason(String yearSeason){
        Integer year = Integer.valueOf(yearSeason.substring(0, 4));
        Integer season = null;
        if (yearSeason.length()  > 4) {
            season = Integer.valueOf(yearSeason.substring(4)) - 1;
        }
        return new YearSeason(year, season);
    }

}
