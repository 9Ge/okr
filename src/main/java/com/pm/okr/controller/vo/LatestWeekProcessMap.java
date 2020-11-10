package com.pm.okr.controller.vo;

import com.pm.okr.controller.BIController;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class LatestWeekProcessMap {
    Map<String, WeekProgress> team = new HashMap<>();
    Map<String, WeekProgress> person = new HashMap<>();
    Map<String, WeekProgress> company = new HashMap<>();
}
