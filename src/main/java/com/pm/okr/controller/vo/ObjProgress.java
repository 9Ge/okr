package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ObjProgress {

    @ApiModelProperty(notes = "objective id")
    String objectiveId;

    @ApiModelProperty(notes = "进度(0~100)")
    Double progress;

    @ApiModelProperty(notes = "状态 0:OFF TRACK; 1:AT RISK; 2:ON TRACK; 3:EXCEEDED")
    Integer status;

    @ApiModelProperty(notes = "期望进度(0~100)")
    Double expected;
}
