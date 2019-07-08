package com.runhang.shadow.server.core.shadow;

import com.runhang.shadow.server.device.entity.ShadowEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author szh
 * @ClassName ShadowFactory
 * @Description 影子管理
 * @Date 2019/2/1 18:09
 **/
@Slf4j
public class ShadowFactory {

    /** 保存影子topic与容器id关系 */
    private static Map<String, String> beanMap = new HashMap<>();

    /**
     * @param dataMap 影子与主题
     * @return 是否成功
     * @Description 批量注入影子
     * @author szh
     * @Date 2019/6/13 14:45
     */
    public static boolean batchInjectShadow(Map<String, ShadowEntity> dataMap) {
        return true;
    }

}
