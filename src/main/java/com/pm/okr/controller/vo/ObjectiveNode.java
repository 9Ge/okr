package com.pm.okr.controller.vo;

import com.pm.okr.controller.ObjectiveController;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ObjectiveNode {

    @ApiModelProperty(notes = "objective 容器")
    Objective.Owner container;

    @ApiModelProperty(notes = "包含的objective")
    List<Objective> objectives;

    @ApiModelProperty(notes = "objective 子容器")
    List<ObjectiveNode> children;
}
