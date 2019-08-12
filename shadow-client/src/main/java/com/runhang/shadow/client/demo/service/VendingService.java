package com.runhang.shadow.client.demo.service;

import com.runhang.shadow.client.core.aspect.ShadowService;
import com.runhang.shadow.client.core.shadow.ShadowUtils;
import com.runhang.shadow.client.demo.entity.result.VendingSimpleInfo;
import com.runhang.shadow.client.device.entity.ShadowEntity;
import com.runhang.shadow.client.device.entity.Vending;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName VendingService
 * @Description 售货机服务
 * @Date 2019/8/8 19:15
 * @author szh
 **/
@Service
@Slf4j
public class VendingService {

    @ShadowService
    public List<VendingSimpleInfo> getVendingList() {
        List<ShadowEntity> shadowEntities = ShadowUtils.getShadowList(Vending.class);
        List<VendingSimpleInfo> vendingList = new ArrayList<>();

        for (ShadowEntity entity : shadowEntities) {
            VendingSimpleInfo info = new VendingSimpleInfo((Vending) entity);
            vendingList.add(info);
        }

        return vendingList;
    }

}
