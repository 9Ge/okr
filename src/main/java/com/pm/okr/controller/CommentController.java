package com.pm.okr.controller;

import com.pm.okr.controller.vo.Comment;
import com.pm.okr.controller.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Deprecated
@CrossOrigin
@Api(tags = "Comment 管理", description = "hidden")
@Controller
@RequestMapping("/{sid}/comment")
public class CommentController {

    @ApiOperation(
            value = "添加 Objective comment",
            notes = "")
    @PostMapping(value = "objective/{objId}", produces = "application/json")
    @ResponseBody
    public Result<Comment, String> addObjectiveContent(
            @ApiParam(value = "会话ID")
            @RequestHeader(required = false) String sid,
            @ApiParam(value = "Objective ID", required = true)
            @PathVariable String objId,
            @ApiParam(value = "comment 内容", required = true)
            @RequestBody String content) {

        return Result.ok(new Comment());
    }

    @ApiOperation(
            value = "添加 Key result comment",
            notes = "")
    @PostMapping(value = "keyResult/{kId}", produces = "application/json")
    @ResponseBody
    public Result<Comment, String> addKeyResultContent(
            @ApiParam(value = "会话ID", required = true)
            @PathVariable String sid,
            @ApiParam(value = "Objective ID", required = true)
            @PathVariable String kId,
            @ApiParam(value = "comment 内容", required = true)
            @RequestBody String content) {

        return Result.ok(new Comment());
    }

    @ApiOperation(
            value = "删除 comment",
            notes = "")
    @DeleteMapping(value = "{commentId}", produces = "application/json")
    @ResponseBody
    public Result<Comment, String> removeContent(
            @ApiParam(value = "会话ID", required = true)
            @PathVariable String sid,
            @ApiParam(value = "comment ID", required = true)
            @PathVariable String commentId) {

        return Result.ok(new Comment());
    }
}
