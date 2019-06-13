package com.runhang.shadow.server.core.model;

import lombok.Data;

import java.util.Map;

/**
 * @ClassName ShadowCode
 * @Description 影子相关生成代码
 * @Date 2019/6/12 21:12
 * @author szh
 **/
@Data
public class ShadowCode {

    // 实体代码
    Map<String, String> entityCode;
    // 数据库映射代码
    Map<String, String> repositoryCode;
    // 初始化类代码
    Map<String, String> initCode;

    public ShadowCode() {
    }

    public ShadowCode(Map<String, String> entityCode, Map<String, String> repositoryCode, Map<String, String> initCode) {
        this.entityCode = entityCode;
        this.repositoryCode = repositoryCode;
        this.initCode = initCode;
    }

}
