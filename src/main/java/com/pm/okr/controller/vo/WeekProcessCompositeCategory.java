package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WeekProcessCompositeCategory {
    @ApiModelProperty(notes = "Team 综合周进度")
    List<WeekProgress> team = new ArrayList<>();
    @ApiModelProperty(notes = "Person 综合周进度")
    List<WeekProgress> person = new ArrayList<>();
    @ApiModelProperty(notes = "Company 综合周进度")
    List<WeekProgress> company = new ArrayList<>();
}
