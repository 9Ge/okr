package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class ManagementUnit {

    @Getter
    @Setter
    @ApiModel("ManagementUnit.PartAdd")
    public static class PartAdd {
        @ApiModelProperty(notes="公司名称")
        String companyName;

        @ApiModelProperty(notes="管理员")
        List<User.PartAdmin> admins;

        @ApiModelProperty(notes="用户数量上限")
        Integer userLimit;
    }

    @ApiModelProperty(notes="管理单元标识")
    String id;

    @ApiModelProperty(notes="用户数量上限")
    Integer userLimit;

    @ApiModelProperty(notes="当前用户数")
    Integer userCount;
    
    @ApiModelProperty(notes="管理公司")
    Company company;

    @ApiModelProperty(notes="管理员")
    List<User.PartBasic> admins;

    @ApiModelProperty(notes="创建人")    
    User.PartShort createUser;
    
    @ApiModelProperty(notes="修改人")
    User.PartShort updateUser;

    @ApiModelProperty(notes="创建时间")
    String createTime;

    @ApiModelProperty(notes="修改时间")
    String updateTime;
}
