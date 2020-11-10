package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Department {
    @ApiModelProperty(notes="唯一标识")
    String id;

    @ApiModelProperty(notes="部门名称")
    String name;
}
