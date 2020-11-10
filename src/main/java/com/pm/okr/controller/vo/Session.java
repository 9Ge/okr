package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@ApiModel(reference="会话", description = "会话信息")
@Getter
@Setter
public class Session {


    @Getter
    @Setter
    @ApiModel("Session.PartPlatform")
    public static class PartPlatform {

        @ApiModelProperty(notes="当前会话用户")
        User.PartShort user;

        @ApiModelProperty(notes="会话ID, 其它接口调用需要传递此ID")
        String sid;

        public PartPlatform(User.PartShort user, String sid) {
            this.user = user;
            this.sid = sid;
        }
    }

    @ApiModelProperty(notes="当前会话用户")
    User.PartDetail user;

    @ApiModelProperty(notes="会话ID, 其它接口调用需要传递此ID")
    String sid;

    public Session(User.PartDetail user, String sid) {
        this.user = user;
        this.sid = sid;
    }
}
