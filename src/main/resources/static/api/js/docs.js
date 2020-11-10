$.views.settings.allowCode(true);
$.views.converters("getResponseModelName", function (val) {
    return getResponseModelName(val);
});

var tempBody = $.templates('#temp_body');
var tempBodyRefModel = $.templates('#temp_body_ref_model');
var tempBodyType = $.templates('#temp_body_type');


//获取context path
var contextPath = getContextPath();


var renderedModel = [];

function getContextPath() {
    var pathName = document.location.pathname;
    var index = pathName.substr(1).indexOf("/");
    var result = pathName.substr(0, index + 1);
    return result;
}

$(function () {
    $.ajax({
        url: "v2/api-docs",
// 	        url : "http://petstore.swagger.io/v2/swagger.json",
        dataType: "json",
        type: "get",
        async: false,
        success: function (data) {
            //layui init
            layui.use(['layer', 'jquery', 'element'], function () {
                var $ = layui.jquery, layer = layui.layer, element = layui.element;
            });
            var jsonData = eval(data);

            $("#title").html(jsonData.info.title);


            var pathArray = [];
            for (var i in jsonData.paths) {
                pathArray.push(i);
            }


            // function getTag(v){
            //     if (v.get){
            //         return v.get.tags[0];
            //     }
            //     if (v.post){
            //         return v.post.tags[0];
            //     }
            //     if (v.put){
            //         return v.put.tags[0];
            //     }
            //     if (v.delete){
            //         return v.delete.tags[0];
            //     }
            // }
            //
            // pathArray = pathArray.sort(function(p1, p2){
            //     if (getTag(jsonData.paths[p1]) > getTag(jsonData.paths[p2])){
            //         return 1
            //     } else if (getTag(jsonData.paths[p1]) < getTag(jsonData.paths[p2])){
            //         return -1
            //     }
            //
            //     if (jsonData.paths[p1].get){
            //         if (jsonData.paths[p2].get){
            //             if (p1 > p2){
            //                 return 1;
            //             } else{
            //                 return -1;
            //             }
            //
            //         } else{
            //             return 1;
            //         }
            //     } else if (jsonData.paths[p1].post){
            //         if (jsonData.paths[p2].post){
            //             if (p1 > p2){
            //                 return 1;
            //             } else{
            //                 return -1;
            //             }
            //         } else if (jsonData.paths[p2].get){
            //             return -1;
            //         } else {
            //             return 1;
            //         }
            //     } else if (jsonData.paths[p1].put){
            //         if (jsonData.paths[p2].put){
            //             if (p1 > p2){
            //                 return 1;
            //             } else{
            //                 return -1;
            //             }
            //         } else if (jsonData.paths[p2].get || jsonData.paths[p2].post){
            //             return -1;
            //         } else {
            //             return 1;
            //         }
            //     } else if (jsonData.paths[p1].delete){
            //         if (jsonData.paths[p2].delete){
            //             if (p1 > p2){
            //                 return 1;
            //             } else{
            //                 return -1;
            //             }
            //         } else if (jsonData.paths[p2].get || jsonData.paths[p2].post || jsonData.paths[p2].put){
            //             return -1;
            //         } else {
            //             return 1;
            //         }
            //     }
            // });
            //
            // var newPaths = {};
            // for (var i = 0; i < pathArray.length; ++i){
            //     var req = jsonData.paths[pathArray[i]];
            //     newPaths[(i + 1) + "#" + pathArray[i]] = req;
            // }
            // jsonData.paths = newPaths;
            $("body").html($("#template").render(jsonData));


            var bodyCache = {};
            var lastOpId;
            $("[name='a_path']").click(function () {
                var path = $(this).attr("path");
                var method = $(this).attr("method");
                var operationId = $(this).attr("operationId");
                $.each(jsonData.paths[path], function (i, d) {
                    if (d.operationId == operationId) {
                        d.path = path;
                        d.method = method;

                        if (lastOpId) {
                            $("#path-body #" + lastOpId).hide();
                        }
                        lastOpId = operationId;
                        if ($("#path-body #" + operationId).length == 0){

                            $("#path-body").append(tempBody.render(d)).find(">div:last").attr("id", operationId);
                        }else{
                            $("#path-body #" + operationId).show();
                        }

                        //如果没有返回值，直接跳过
                        if (!d.responses["200"].hasOwnProperty("schema")) {
                            // continue
                            return true;
                        }

                        //基本类型
                        if (d.responses["200"]["schema"].hasOwnProperty("type")) {
                            var model = {"type": d.responses["200"]["schema"]["type"]};
                            $("#" + operationId + "#path-body-response-model").append(tempBodyType.render(model));
                            // continue
                            return true;
                        }

                        //引用类型
                        var modelName = getRefName(d.responses["200"]["schema"]["$ref"]);
                        if (d.parameters) {
                            $.each(d.parameters, function (i, p) {
                                if (p["schema"]) {
                                    var parameterModelName = getRefName(p["schema"]["$ref"]);
                                    renderRefModel(operationId,"path-body-request-model", jsonData, parameterModelName);
                                }
                            });
                        }
                        renderRefModel(operationId,"path-body-response-model", jsonData, modelName);
                        renderedModel = [];
                    }
                });
            });

            //提交测试按钮
            $("[name='btn_submit']").click(function () {
                var operationId = $(this).attr("operationId");
                var parameterJson = {};
                $("input[operationId='" + operationId + "']").each(function (index, domEle) {
                    var k = $(domEle).attr("name");
                    var v = $(domEle).val();
                    parameterJson.push({k: v});
                });
            });
        }
    });

});

/**
 * 渲染ref类型参数
 * @param domId 需要添加的domId
 * @param jsonData
 * @param modelName
 */
function renderRefModel(operationId, domId, jsonData, modelName) {
    if (modelName) {
        if (renderedModel.indexOf(modelName) >= 0) {
            return;
        }
        renderedModel.push(modelName);

        var model = jsonData.definitions[modelName];
        // var i = modelName.indexOf("#");
        // if (i > 0){
        //     modelName = modelName.substring(0, i);
        // }
        model.name = modelName;
        model.domId = domId;
        //修改有嵌套对象的type
        $.each(model.properties, function (i, v) {
            if (v.items) {
                $.each(v.items, function (j, item) {
                    var typeModel = item.startsWith("#") ? getRefName(item) : item;
                    // var i = typeModel.indexOf("#");
                    // if (i > 0){
                    //     typeModel = typeModel.substring(0, i);
                    // }
                    model.properties[i].type = typeModel + "[]";
                });
            }

            //自定义对象类型（非Array）
            if (!v.type) {
                model.properties[i].type = getRefName(v["$ref"]);
            }
        });
        //如果该对象没有被渲染到页面，则渲染
        if ($("#" + operationId + " #ref-" + domId + "-" + modelName).length == 0) {
            $("#" + operationId + " #" + domId).append(tempBodyRefModel.render(model));
        }

        //递归渲染多层对象嵌套
        $.each(model.properties, function (i, v) {
            //Array
            if (v.items) {
                $.each(v.items, function (j, item) {

                    if (item.startsWith("#")) {
                        renderRefModel(operationId, domId, jsonData, getRefName(item));
                    }
                });
            }

            //单个对象引用
            if (v.hasOwnProperty("$ref")) {
                renderRefModel(operationId, domId, jsonData, getRefName(v["$ref"]));
            }

        });
    }
}

//获得模型名字
function getRefName(val) {
    if (!val) {
        return null;
    }
    return val.substring(val.lastIndexOf("/") + 1, val.length);
}

//测试按钮，获取数据
function getData(operationId) {
    var path = contextPath + $("[m_operationId='" + operationId + "']").attr("path");
    //path 参数
    $("[p_operationId='" + operationId + "'][in='path']").each(function (index, domEle) {
        var k = $(domEle).attr("name");
        var v = $(domEle).val();
        if (v) {
            path = path.replace("{" + k + "}", v);
        }
    });

    //header参数
    var headerJson = {};
    $("[p_operationId='" + operationId + "'][in='header']").each(function (index, domEle) {
        var k = $(domEle).attr("name");
        var v = $(domEle).val();
        if (v) {
            headerJson[k] = v;
        }
    });

    //请求方式
    var parameterType = $("#content_type_" + operationId).val();

    //query 参数
    var parameterJson = {};
    if ("form" == parameterType) {
        $("[p_operationId='" + operationId + "'][in='query']").each(function (index, domEle) {
            var k = $(domEle).attr("name");
            var v = $(domEle).val();
            if (v) {
                parameterJson[k] = v;
            }
        });
    } else if ("json" == parameterType) {
        var str = $("#text_tp_" + operationId).val();
        try {
            parameterJson = JSON.parse(str);
        } catch (error) {
            layer.msg("" + error, {icon: 5});
            return false;
        }
    }

    //发送请求
    send(path, operationId, headerJson, parameterJson);
}


/**
 * 请求类型
 */
function changeParameterType(el) {
    var operationId = $(el).attr("operationId");
    var type = $(el).attr("type");
    $("#content_type_" + operationId).val(type);
    $(el).addClass("layui-btn-normal").removeClass("layui-btn-primary");
    if ("form" == type) {
        $("#text_tp_" + operationId).hide();
        $("#table_tp_" + operationId).show();
        $("#pt_json_" + operationId).addClass("layui-btn-primary").removeClass("layui-btn-normal");
    } else if ("json" == type) {
        $("#text_tp_" + operationId).show();
        $("#table_tp_" + operationId).hide();
        $("#pt_form_" + operationId).addClass("layui-btn-primary").removeClass("layui-btn-normal");
    }
}

/**
 * 发送请求
 * @param url 地址
 * @param operationId   operationId
 * @param header    header参数
 * @param data  data数据
 */
function send(url, operationId, header, data) {

    var type = $("[m_operationId='" + operationId + "']").attr("method");

    //是否有formData类型数据
    var hasFormData = $("[p_operationId='" + operationId + "'][in='formData']").length >= 1;

    //是否有body类型数据
    var hasBody = $("[p_operationId='" + operationId + "'][in='body']").length >= 1;

    var options = {withQuotes: false};

    //发送请求
    if (hasFormData) {
        var formData = new FormData($("#form_" + operationId)[0]);
        $.ajax({
            type: type,
            url: encodeURI(url),
            headers: header,
            data: formData,
            dataType: 'json',
            cache: false,
            processData: false,
            contentType: false,
            success: function (data) {
                $("#" + operationId + " #json-response").parent().prev().find("legend").css("color", "green");
                $("#" + operationId + " #json-response").jsonViewer(data, options);
            },
            error: function (e) {
                $("#" + operationId + " #json-response").parent().prev().find("legend").css("color", "red");
                $("#" + operationId + " #json-response").jsonViewer(e);
                // layer.msg("" + JSON.stringify(e), {icon: 5});
            }
        });
        return;
    }

    //querystring ,将参数加在url后面
    url = appendParameterToUrl(url, data);

    //requestBody 请求
    var bodyData;
    if (hasBody) {
        var dom = $("[p_operationId='" + operationId + "'][in='body']")[0];
        bodyData = $(dom).val();
    }
    var contentType = $("#consumes_" + operationId).text();

    $.ajax({
        type: type,
        url: encodeURI(url),
        headers: header,
        data: bodyData,
        dataType: 'json',
        contentType: contentType,
        success: function (data) {
            $("#" + operationId + " #json-response").parent().prev().find("legend").css("color", "green");
            $("#" + operationId + " #json-response").jsonViewer(data, options);
        },
        error: function (e) {
            $("#" + operationId + " #json-response").parent().prev().find("legend").css("color", "red");
            $("#" + operationId + " #json-response").jsonViewer(e);
            // layer.msg("" + JSON.stringify(e), {icon: 5});
        }
    });

}

/**
 * 给url拼装参数
 * @param url
 * @param parameter
 */
function appendParameterToUrl(url, parameter) {
    if ($.isEmptyObject(parameter)) {
        return url;
    }
    $.each(parameter, function (k, v) {
        if (url.indexOf("?") == -1) {
            url += "?";
        }
        url += k;
        url += "=";
        url += v;
        url += "&";
    });
    return url.substring(0, url.length - 1);
}