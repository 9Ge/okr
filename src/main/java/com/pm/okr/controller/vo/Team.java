package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class Team {

    @Getter
    @Setter
    @ApiModel("Team.Part")
    public static class Part {

        @ApiModelProperty(notes = "唯一标识")
        String id;

        @ApiModelProperty(notes = "团队名称")
        String name;

        @ApiModelProperty(notes = "颜色")
        String color;
    }

    @Getter
    @Setter
    @ApiModel("Team.PartAdd")
    public static class PartAdd {

        @ApiModelProperty(notes = "团队名称")
        String name;

        @ApiModelProperty(notes = "颜色")
        String color;
    }

    @ApiModelProperty(notes = "唯一标识")
    String id;

    @ApiModelProperty(notes = "团队名称")
    String name;

    @ApiModelProperty(notes = "颜色")
    String color;

    @ApiModelProperty(notes = "团队管理员IDs")
    List<String> managers;

    @ApiModelProperty(notes = "团队观察员IDs")
    List<String> observers;

    @ApiModelProperty(notes = "部门（可选）")
    Department department;

    @ApiModelProperty(notes = "团队成员")
    List<User.PartDetail> members = new ArrayList<>();

    public Team(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Team() {
    }

}
