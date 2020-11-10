package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("结果")
public class Result<O, F> {


    @ApiModelProperty(notes = "成功结果，如果失败，该值为null")
    O ok;

    @ApiModelProperty(notes = "错误信息，如果成功，该值为null")
    F fail;

    @ApiModelProperty(notes = "统一错误码")
    ErrorCode code;

    Result(ErrorCode code, O ok, F fail) {
        this.code = code;
        this.ok = ok;
        this.fail = fail;
    }


    public static <O, F> Result<O, F> ok(O o) {
        return new Result<>(ErrorCode.OK, o, null);
    }

    public static <F> Result<String, F> ok() {
        return ok("OK");
    }

    public static <O, F> Result<O, F> fail(ErrorCode code, F f) {
        return new Result<>(code, null, f);
    }

    public static <O, F> Result<O, F> fail(F f) {
        return fail(ErrorCode.FAILED, f);
    }
}
