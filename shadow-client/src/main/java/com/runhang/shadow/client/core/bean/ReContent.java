package com.runhang.shadow.client.core.bean;

import com.runhang.shadow.client.core.enums.ReErrorCode;
import lombok.Data;

/**
 * @ClassName ReContent
 * @Description 影子通信回复内容
 * @Date 2019/4/28 21:02
 * @author szh
 **/
@Data
public class ReContent {

    /** 错误编号 **/
    private String errorCode;
    /** 错误描述 **/
    private String errorMsg;

    public ReContent() {

    }

    public ReContent(ReErrorCode reErrorCode) {
        this.errorCode = reErrorCode.getErrorCode();
        this.errorMsg = reErrorCode.getErrorMsg();
    }

}
