package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ObjectiveFilter {

    @ApiModelProperty(notes = "进度 0:0~100%; 1:0~33%; 2:34~66%; 3:67~100%")
    Integer range = 0;

    public static class Progress{
        public final static int ALL = 0;
        public final static int P33 = 1;
        public final static int P66 = 2;
        public final static int P100 = 3;
    }

    @ApiModelProperty(notes = "状态 0:ALL; 1:OFF TRACK; 2:AT RISK; 3:ON TRACK; 4:EXCEEDED;")
    Integer status = 0;

    public static class Status{
        public final static int ALL = 0;
        public final static int OFF_TRACK = 1;
        public final static int AT_RISK = 2;
        public final static int ON_TRACK = 3;
        public final static int EXCEEDED = 4;
    }

    @ApiModelProperty(notes = "KeyResult 0:All; 1:2周前; 2:4周前; 3:8周前")
    Integer krs = 0;

    public static class KR{
        public final static int ALL = 0;
        public final static int WEEK2 = 1;
        public final static int WEEK4 = 2;
        public final static int WEEK8 = 3;
    }
}
