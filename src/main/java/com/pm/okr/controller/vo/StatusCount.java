package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusCount {

    @ApiModelProperty(notes = "OFF TRACK 数量")
    Integer offTrackCount = 0;

    @ApiModelProperty(notes = "AT RISK 数量")
    Integer atRiskCount = 0;

    @ApiModelProperty(notes = "ON TRACK 数量")
    Integer onTrackCount = 0;

    @ApiModelProperty(notes = " EXCEEDED 数量")
    Integer exceededCount = 0;
}
