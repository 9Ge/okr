package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkedCount {

    @ApiModelProperty(notes = "Team Objective 有链接数量")
    Integer teamLinkedCount;

    @ApiModelProperty(notes = "Team Objective 无链接数量")
    Integer teamNotLinkedCount;

    @ApiModelProperty(notes = "User Objective 有链接数量")
    Integer personLinkedCount;

    @ApiModelProperty(notes = "User Objective 无链接数量")
    Integer personNotLinkedCount;
}
