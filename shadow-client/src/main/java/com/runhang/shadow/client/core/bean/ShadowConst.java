package com.runhang.shadow.client.core.bean;

/**
 * 影子中的常量字段
 */
public interface ShadowConst {

    // RePayload 影子通信回复载荷状态
    String PAYLOAD_STATUS_SUCCESS = "success";
    String PAYLOAD_STATUS_ERROR = "error";

    // ShadowDoc 影子文档时间戳key
    String DOC_KEY_TIMESTAMP = "timestamp";

    // ShadowOpsBean 操作字段
    String OPERATION_METHOD_DELETE = "delete";
    String OPERATION_METHOD_UPDATE = "update";
    String OPERATION_METHOD_GET = "get";

    // ShadowReplyBean 回复操作字段
    String REPLY_METHOD_REPLY = "reply";
    String REPLY_METHOD_CONTROL = "control";

}
