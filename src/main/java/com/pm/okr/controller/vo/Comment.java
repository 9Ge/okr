package com.pm.okr.controller.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {
    String id;
    String content;
    User.PartShort creator;
    String createTime;

}
