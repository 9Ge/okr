package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeekProgress {

    @ApiModelProperty(notes="Objective Id", hidden = true)
    String objId;

    @ApiModelProperty(notes="年份")
    Integer year;

    @ApiModelProperty(notes="季度(从0开始)")
    Integer season;

    @ApiModelProperty(notes="周(从0开始)")
    Integer week;

    @ApiModelProperty(notes = "进度(0~100)")
    Double progress;

    @ApiModelProperty(notes = "状态 0:OFF TRACK; 1:AT RISK; 2:ON TRACK; 3:EXCEEDED")
    Integer status;

    @ApiModelProperty(notes = "期望进度(0~100)")
    Double expected;

    public static class Status{
        public final static int OFF_TRACK = 0;
        public final static int AT_RISK = 1;
        public final static int ON_TRACK = 2;
        public final static int EXCEEDED = 3;
    }

}
