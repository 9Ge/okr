package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


import java.util.List;


@Getter
@Setter
public class Company {

    @Getter
    @Setter
    @ApiModel("Company.Part")
    public static class Part {
        @ApiModelProperty(notes = "唯一标识")
        String id;

        @ApiModelProperty(notes = "公司名称")
        String name;
    }

    @ApiModelProperty(notes = "唯一标识")
    String id;


    @ApiModelProperty(notes = "公司名称")
    String name;


    @ApiModelProperty(notes = "公司团队")
    List<Team> teams;


    public Company(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Company() {
    }

}
