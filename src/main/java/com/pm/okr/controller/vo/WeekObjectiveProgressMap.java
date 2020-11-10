package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeekObjectiveProgressMap {
    @ApiModelProperty(notes = "Objective ID")
    String key;

    @ApiModelProperty(notes = "周进度")
    List<WeekProgress> value;
}
