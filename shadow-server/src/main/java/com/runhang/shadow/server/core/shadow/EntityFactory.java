package com.runhang.shadow.server.core.shadow;

import com.runhang.shadow.client.common.utils.BeanUtils;
import com.runhang.shadow.server.device.entity.ShadowEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName EntityFactory
 * @Description 实体相关操作
 * @Date 2019/7/4 9:38
 * @author szh
 **/
public class EntityFactory {

    /**
     * 保存实体的SRI
     */
    private static Set<String> entitySriSet = new HashSet<>();

    /**
     * @Description 递归注入所有实体
     * @param shadowEntity 实体
     * @param topic 主题
     * @param entityNames 所有实体类名
     * @author szh
     * @Date 2019/6/18 11:23
     */
    public static void injectEntities(ShadowEntity shadowEntity, String topic,
                                      List<String> entityNames) {

    }

    /**
     * @Description 清空实体
     * @author szh
     * @Date 2019/6/18 0:09
     */
    public static void destroyEntities() {
        for (String beanName : entitySriSet) {
            BeanUtils.destroyBean(beanName);
        }
        entitySriSet.clear();
    }

}
