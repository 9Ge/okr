package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Objective {

    @Getter
    @Setter
    @ApiModel("Objective.Owner")
    public static class Owner{
        @ApiModelProperty(notes="Objective 归属用户")
        User.PartShort user;

        @ApiModelProperty(notes="Objective 归属团队")
        Team.Part team;

        @ApiModelProperty(notes="Objective 归属公司")
        Company.Part company;
    }

    @Getter
    @Setter
    @ApiModel("Objective.PartToLink")
    public static class PartToLink {
        @ApiModelProperty(notes="唯一标识")
        String id;

        @ApiModelProperty(notes="描述内容")
        String content;

        @ApiModelProperty(notes="颜色")
        String color;

        @ApiModelProperty(notes="Objective 拥有者")
        Owner owner;
    }

    @Getter
    @Setter
    @ApiModel("Objective.LinkPart")
    public static class LinkPart{
        @ApiModelProperty(notes="唯一标识")
        String id;

        @ApiModelProperty(notes="目标所属年份")
        Integer year;

        @ApiModelProperty(notes="目标所属季度: 0:一季度;1:二季度;2:三季度;3四季度;")
        Integer season;

        @ApiModelProperty(notes="颜色")
        String color;

        @ApiModelProperty(notes = "进度(0~100)")
        Double progress;

        @ApiModelProperty(notes = "权重")
        Double weight = 1d;

        @ApiModelProperty(notes="创建时间")
        String createTime;

        @ApiModelProperty(notes="被分配的KR的Objective ID")
        Integer assignFrom;

        @ApiModelProperty(notes="描述内容")
        String content;

        @ApiModelProperty(notes="Objective 向上链接创建时间")
        String linkTime;

        @ApiModelProperty(notes="Objective 拥有者")
        Owner owner;
    }

    @Getter
    @Setter
    @ApiModel("Objective.PartAdd")
    public static class PartAdd{
        @ApiModelProperty(notes="唯一标识")
        String id;

        @ApiModelProperty(notes="目标所属年份")
        Integer year;

        @ApiModelProperty(notes="目标所属季度: 0:一季度;1:二季度;2:三季度;3四季度;")
        Integer season;

        @ApiModelProperty(notes="颜色")
        String color;

        @ApiModelProperty(notes = "进度(0~100)")
        Double progress;

        @ApiModelProperty(notes = "权重")
        Double weight = 1d;

        @ApiModelProperty(notes="创建时间")
        String createTime;

        @ApiModelProperty(notes="Objective 拥有者")
        Owner owner;
    }

    @ApiModelProperty(notes="唯一标识")
    String id;

    @ApiModelProperty(notes="描述内容")
    String content;

    @ApiModelProperty(notes="颜色")
    String color;

    @ApiModelProperty(notes="目标所属年份")
    Integer year;

    @ApiModelProperty(notes="目标所属季度: 0:一季度;1:二季度;2:三季度;3四季度;")
    Integer season;

//    @ApiModelProperty(notes="注释说明")
//    List<Comment> comments;

    @ApiModelProperty(notes = "进度(0~100)")
    Double progress;

    @ApiModelProperty(notes = "权重")
    Double weight = 1d;

    @ApiModelProperty(notes="创建时间")
    String createTime;

    @ApiModelProperty(notes="标记 Objective 是否归档")
    Boolean archived;

    @ApiModelProperty(notes="Objective 拥有者")
    Owner owner;

    @ApiModelProperty(notes="被分配的KR的Objective ID")
    Integer assignFrom;

    @ApiModelProperty(notes="Objective 向上链接创建时间")
    String linkTime;

    @ApiModelProperty(notes = "向上链接 Objective")
    Objective.LinkPart linkedAbove;

    @ApiModelProperty(notes="向下链接")
    List<LinkPart> linkedBelow;

    @ApiModelProperty(notes="Objective 包含的 key result")
    List<KeyResult> keyResults;
}
