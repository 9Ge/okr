package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Link {

    @ApiModelProperty(notes = "链接一端Objective")
    Objective.LinkPart objective;

    @ApiModelProperty(notes = "链接创建时间")
    String createTime;

}
