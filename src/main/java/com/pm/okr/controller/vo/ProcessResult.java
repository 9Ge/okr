package com.pm.okr.controller.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@ApiModel("结果")
public class ProcessResult extends Result<Map<String, WeekProgress>, String> {


    ProcessResult(ErrorCode code, Map<String, WeekProgress> ok, String fail) {
        super(code, ok, fail);
    }
}
