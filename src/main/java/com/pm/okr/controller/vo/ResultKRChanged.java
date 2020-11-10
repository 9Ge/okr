package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResultKRChanged {

    @ApiModelProperty("Key result")
    KeyResult.PartShort KR;

    @ApiModelProperty("KR变化影响的Objective")
    List<ObjProgress> objectiveProgress;
}
