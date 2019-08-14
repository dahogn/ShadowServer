package com.runhang.shadow.client.demo.service;

import com.runhang.shadow.client.core.aspect.ShadowService;
import com.runhang.shadow.client.core.shadow.ShadowUtils;
import com.runhang.shadow.client.demo.entity.param.CommodityParam;
import com.runhang.shadow.client.demo.entity.result.CommoditySimpleInfo;
import com.runhang.shadow.client.device.entity.CargoRoad;
import com.runhang.shadow.client.device.entity.Commodity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName CommodityService
 * @Description 商品service
 * @Date 2019/8/12 17:13
 * @author szh
 **/
@Service
@Slf4j
public class CommodityService {

    /**
     * @Description 获取货道内商品
     * @param cargoRoadId 货道sri
     * @return 商品列表
     * @author szh
     * @Date 2019/8/12 17:18
     */
    @ShadowService
    public List<CommoditySimpleInfo> getCommodityList(String cargoRoadId) {
        List<CommoditySimpleInfo> commodityList = new ArrayList<>();
        CargoRoad cargoRoad = (CargoRoad) ShadowUtils.getEntity(cargoRoadId);

        if (null != cargoRoad && null != cargoRoad.getCommodity()) {
            for (Commodity commodity : cargoRoad.getCommodity()) {
                CommoditySimpleInfo info = new CommoditySimpleInfo(commodity);
                commodityList.add(info);
            }
        }

        return commodityList;
    }

    /**
     * @Description 修改商品列表
     * @param param 商品数据
     * @author szh
     * @Date 2019/8/14 14:02
     */
    @ShadowService
    public String editCommodityList(CommodityParam param) {
        CargoRoad cargoRoad = (CargoRoad) ShadowUtils.getEntity(param.getCargoRoadId());
        List<Commodity> newCommodityList = param.getCommodityList(cargoRoad.getEntityTopic());
        List<Commodity> oldCommodityList = cargoRoad.getCommodity();
        deleteOldCommodity(newCommodityList, oldCommodityList);
        addNewCommodity(newCommodityList, oldCommodityList);
        editCommodity(newCommodityList, oldCommodityList);

        try {
            ShadowUtils.commitAndPush(cargoRoad.getEntityTopic());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "success";
    }

    /**
     * @Description 查找多余的
     * @param more 多的
     * @param less 少的
     * @return 多余的商品
     * @author szh
     * @Date 2019/8/14 14:03
     */
    private List<Commodity> findExtraCommodity(List<Commodity> more, List<Commodity> less) {
        if (null == more || more.isEmpty()) {
            return null;
        }
        List<Commodity> extra;
        if (null == less || less.isEmpty()) {
            extra = more;
        } else {
            extra = more.stream().filter(item -> !less.contains(item)).collect(Collectors.toList());
        }
        return extra;
    }

    /**
     * @Description 删除已删除的商品
     * @param newCommodityList 页面发送的商品
     * @param oldCommodityList 内存中的商品
     * @author szh
     * @Date 2019/8/14 14:03
     */
    private void deleteOldCommodity(List<Commodity> newCommodityList, List<Commodity> oldCommodityList) {
        List<Commodity> toDelete = findExtraCommodity(oldCommodityList, newCommodityList);
        if (null != toDelete) {
            oldCommodityList.removeAll(toDelete);
        }
    }

    /**
     * @Description 增加新增的商品
     * @param newCommodityList 页面发送的商品
     * @param oldCommodityList 内存中的商品
     * @author szh
     * @Date 2019/8/14 14:04
     */
    private void addNewCommodity(List<Commodity> newCommodityList, List<Commodity> oldCommodityList) {
        List<Commodity> toAdd = findExtraCommodity(newCommodityList, oldCommodityList);
        if (null != toAdd) {
            for (Commodity commodity : toAdd) {
                try {
                    ShadowUtils.addEntity(commodity);
                    oldCommodityList.add(commodity);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    /**
     * @Description 修改原有的商品
     * @param newCommodityList 页面发送的商品
     * @param oldCommodityList 内存中的商品
     * @author szh
     * @Date 2019/8/14 14:04
     */
    private void editCommodity(List<Commodity> newCommodityList, List<Commodity> oldCommodityList) {
        List<Commodity> same = newCommodityList.stream().filter(oldCommodityList::contains).collect(Collectors.toList());
        for (Commodity newSame : same) {
            Commodity oldSame = oldCommodityList.get(oldCommodityList.indexOf(newSame));
            if (!oldSame.getName().equals(newSame.getName())) {
                oldSame.setName(newSame.getName());
            }
            if (!oldSame.getNumber().equals(newSame.getNumber())) {
                oldSame.setNumber(newSame.getNumber());
            }
            if (oldSame.getPrice() != newSame.getPrice()) {
                oldSame.setPrice(newSame.getPrice());
            }
        }
    }

}
