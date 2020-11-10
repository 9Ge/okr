package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KeyResult {


    @Getter
    @Setter
    @ApiModel("KeyResult.PartShort")
    public static class PartShort{
        @ApiModelProperty(notes="唯一标识")
        String id;

        @ApiModelProperty(notes="进度(0~100)")
        Double progress;

        @ApiModelProperty(notes = "权重")
        Double weight;

        @ApiModelProperty(notes = "结果类型")
        String resultType;

        @ApiModelProperty(notes = "范围起始值")
        Double startValue;

        @ApiModelProperty(notes = "范围结束值")
        Double targetValue;

        @ApiModelProperty(notes = "保留小数位数")
        Integer decimals;
    }

    @ApiModelProperty(notes="唯一标识")
    String id;

    @ApiModelProperty(notes="Objective ID")
    String objId;

    @ApiModelProperty(notes="进度(0~100)")
    Double progress;

    @ApiModelProperty(notes="描述内容")
    String content;

//    @ApiModelProperty(notes="注释说明")
//    List<Comment> comments;

    @ApiModelProperty(notes = "权重")
    Double weight;

    @ApiModelProperty(notes="创建时间")
    String createTime;

    @ApiModelProperty(notes="最后更新时间")
    String updateTime;

    @ApiModelProperty(notes = "结果类型")
    String resultType;

    @ApiModelProperty(notes = "范围起始值")
    Double startValue;

    @ApiModelProperty(notes = "范围结束值")
    Double targetValue;

    @ApiModelProperty(notes = "保留小数位数")
    Integer decimals;

}
