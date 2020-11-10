package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ObjWeekProgress {

    @ApiModelProperty(notes = "objective id")
    String objectiveId;

    @ApiModelProperty(notes = "objective 进度")
    List<WeekProgress> progresses;
}
