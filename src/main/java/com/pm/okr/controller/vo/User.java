package com.pm.okr.controller.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;


@Getter
@Setter
public class User {

    @Getter
    @Setter
    @ApiModel("User.PartShort")
    public static class PartShort {

        @ApiModelProperty(notes = "唯一标识")
        String id;

        @ApiModelProperty(notes = "用户名")
        String name;

        @ApiModelProperty(notes = "角色:  ADMIN/USER  逗号分隔")
        String role;

        @ApiModelProperty(notes = "颜色")
        String color;
    }

    @Getter
    @Setter
    @ApiModel("User.PartShort")
    public static class PartBasic {

        @ApiModelProperty(notes = "唯一标识")
        String id;

        @ApiModelProperty(notes = "用户名")
        String name;

        @ApiModelProperty(notes = "角色:  ADMIN/USER  逗号分隔")
        String role;

        @ApiModelProperty(notes = "颜色")
        String color;

        @ApiModelProperty(notes = "电子邮件")
        String email;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ApiModel("User.PartDetail")
    public static class PartDetail {

        @ApiModelProperty(notes = "唯一标识")
        String id;

        @ApiModelProperty(notes = "用户名")
        String name;

        @ApiModelProperty(notes = "角色:  ADMIN/USER  逗号分隔")
        String role;

        @ApiModelProperty(notes = "颜色")
        String color;

        @ApiModelProperty(notes = "电子邮件")
        String email;

        @ApiModelProperty(notes = "团队")
        List<Team.Part> team;
    }

    @Getter
    @Setter
    @ApiModel("User.PartEditable")
    public static class PartEditable {

        @ApiModelProperty(notes = "唯一标识")
        String id;

        @ApiModelProperty(notes = "用户名")
        String name;

        @ApiModelProperty(notes = "角色:  ADMIN/USER  逗号分隔")
        String role;

        @ApiModelProperty(notes = "颜色")
        String color;

        @ApiModelProperty(notes = "电子邮件")
        String email;

        @ApiModelProperty(notes = "密码")
        String psw;

        @ApiModelProperty(notes = "原密码")
        String oldPsw;
    }


    @Getter
    @Setter
    @ApiModel("User.PartAdmin")
    public static class PartAdmin {

        @ApiModelProperty(notes = "用户名")
        String name;

        @ApiModelProperty(notes = "颜色")
        String color;

        @ApiModelProperty(notes = "电子邮件")
        String email;

        @ApiModelProperty(notes = "密码")
        String psw;
    }

    @Getter
    @Setter
    @ApiModel("User.PartRegister")
    public static class PartRegister {

        @ApiModelProperty(notes = "用户名")
        String name;

        @ApiModelProperty(notes = "颜色")
        String color;

        @ApiModelProperty(notes = "角色:  ADMIN/USER  逗号分隔")
        String role;

        @ApiModelProperty(notes = "电子邮件")
        String email;

        @ApiModelProperty(notes = "密码")
        String psw;

        @ApiModelProperty(notes = "用户所属团队")
        List<Integer> teams;
    }

    @ApiModelProperty(notes = "唯一标识")
    String id;

    @ApiModelProperty(notes = "用户名")
    String name;

    @ApiModelProperty(notes = "颜色")
    String color;

    @ApiModelProperty(notes = "角色:  ADMIN/USER  逗号分隔")
    String role;

    @ApiModelProperty(notes = "电子邮件")
    String email;

    @ApiModelProperty(notes = "密码")
    String psw;

    @ApiModelProperty(notes = "用户所属团队")
    List<Team.Part> team;
}
