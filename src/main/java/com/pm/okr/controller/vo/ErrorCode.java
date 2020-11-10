package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModelProperty;


public class ErrorCode {

    public final static ErrorCode OK = ec(0);
    public final static ErrorCode FAILED = ec(1);
    public final static ErrorCode LINK_EXIST = ec(2);
    public final static ErrorCode LINK_BELOW_NOT_ALLOWED = ec(3);
    public final static ErrorCode TEAM_EXISTS = ec(4);
    public final static ErrorCode NOT_LOGIN = ec(5);
    public final static ErrorCode USER_PSW_ERROR = ec(6);

    @ApiModelProperty(notes = "0 : 成功<br/>" +
            "1 : 操作失败<br/>" +
            "2 : 链接已存在<br/>" +
            "3 : 不能允许链接到下级<br/>" +
            "4 : 团队已存在<br/>" +
            "5 : 未登录<br/>" +
            "6 : 用户名或密码错误<br/>")
    Integer value;

    static ErrorCode ec(Integer v) {
        return new ErrorCode(v);
    }

    ErrorCode(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
