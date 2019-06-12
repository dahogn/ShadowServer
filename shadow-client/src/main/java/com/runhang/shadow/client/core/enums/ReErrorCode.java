package com.runhang.shadow.client.core.enums;

/**
 * 影子返回结果错误码
 */
public enum ReErrorCode {

    WRONG_JSON_FORMAT("400", "不正确的JSON格式"),
    LACK_MATHOD("401", "影子JSON缺少method信息"),
    LACK_STATE("402", "影子JSON缺少state字段"),
    VERSION_NOT_NUM("403", "影子JSON version不是数字"),
    LACK_REPORTED("404", "影子JSON缺少reported字段"),
    REPORTED_IS_NULL("405", "影子JSON reported属性字段为空"),
    METHOD_INVALID("406", "影子JSON method是无效的方法"),
    CONTENT_IS_NULL("407", "影子内容为空"),
    REPORTED_OVER_MAX("408", "影子reported属性个数超过128个"),
    VERSION_CONFLIC("409", "影子版本冲突"),
    SHADOW_ATTR_WRONG("500", "影子属性不存在"),
    SHADOW_IS_WRITING("501", "影子正在写入"),
    SHADOW_ATTR_NOT_MODIFIED("502", "影子属性未修改"),
    SERVER_ERROR("503", "服务端处理异常");

    private String errorCode;
    private String errorMsg;

    ReErrorCode(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

}
