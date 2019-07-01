package com.runhang.shadow.client.core.bean.shadow;

import com.alibaba.fastjson.JSONObject;
import com.runhang.shadow.client.common.utils.ClassUtils;
import com.runhang.shadow.client.core.bean.comm.ShadowConst;
import com.runhang.shadow.client.core.enums.EntityOperation;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.model.ShadowField;
import com.runhang.shadow.client.device.entity.ShadowEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @ClassName ShadowBean
 * @Description 影子管理对象
 * @Date 2019/4/28 20:39
 * @author szh
 **/
@Slf4j
@Data
public class ShadowBean {

    /** 影子读写锁 **/
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    /** 影子主题 **/
    private String topic;
    /** 影子对象 **/
    private ShadowEntity data;
    /** 影子文档 **/
    private ShadowDoc doc;
    /**
     * 影子变更属性
     * key sri
     * value：变更属性
     */
    private Map<String, ShadowField> shadowField = new HashMap<>();

    public ShadowBean() {
        ShadowDoc doc = new ShadowDoc();
        doc.setState(new ShadowDocState());
        doc.setMetadata(new ShadowDocMetadata());
        setDoc(doc);
    }

    public void setData(ShadowEntity data) {
        this.data = data;
        initDocState();
    }
    
    /**
     * @Description 初始化state中reported部分
     * @author szh
     * @Date 2019/7/1 14:08       
     */
    private void initDocState() {
        if (null != data) {
            doc.getState().setReported(data);
        }
    }

    /**
     * @Description 增加变更属性
     * @param field 属性信息
     * @author szh
     * @Date 2019/6/19 14:28
     */
    public void addModifiedField(ShadowField field) {
        if (shadowField.keySet().contains(field.getSri())) {
            Map<String, Object> f = shadowField.get(field.getSri()).getField();
            f.putAll(field.getField());
        } else {
            shadowField.put(field.getSri(), field);
        }
    }

    /**
     * @Description 清空变更属性
     * @author szh
     * @Date 2019/6/19 14:28
     */
    public void clearModifiedField() {
        shadowField.clear();
    }

    /**
     * @Description 由设备端给定值更新影子
     * @param updateValue 更新属性
     * @return 错误信息
     * @author szh
     * @Date 2019/5/2 15:55
     */
    public ReErrorCode updateShadowByDevice(Map<String, Object> updateValue) {
        /*// 判断是否加了写锁
        if (rwLock.isWriteLocked()) {
            return ReErrorCode.SHADOW_IS_WRITING;
        }
        rwLock.writeLock().lock();
        try {
            // 更新影子属性
            for (String key : updateValue.keySet()) {
                boolean updateSuccess = ClassUtils.setValue(data, key, updateValue.get(key));
                if (!updateSuccess) {
                    // 影子属性回退
                    shadowRevert();
                    return ReErrorCode.SHADOW_ATTR_WRONG;
                }
            }
            // 更新文档属性
            long current = System.currentTimeMillis();
            // 反序列化影子状态
            Map<String, Object> state = doc.getState().getReported();
            for (String key : updateValue.keySet()) {
                // state
                state.put(key, updateValue.get(key));
                // metadata
                ShadowDocMetadata metadata = doc.getMetadata();
                if (null == metadata) {
                    metadata = new ShadowDocMetadata();
                }
                Map<String, Object> metadataTime = new HashMap<>();
                metadataTime.put(ShadowConst.DOC_KEY_TIMESTAMP, current);
                metadata.getReported().put(key, metadataTime);
                doc.setMetadata(metadata);
            }
            // 序列化保存状态
            doc.getState().setReported(state);
            // 更新时间戳
            doc.setTimestamp(current);
            return null;
        } catch (Exception e) {
            log.error("影子更新失败：" + e.getMessage());
            return ReErrorCode.SERVER_ERROR;
        } finally {
            rwLock.writeLock().unlock();
        }*/
        return null;
    }

    /**
     * @Description 设备端更新影子成功
     * @return 是否成功
     * @author szh
     * @Date 2019/5/3 11:27
     */
    public ReErrorCode updateShadowByDevice() {
        // 判断是否加了写锁
        if (rwLock.isWriteLocked()) {
            return ReErrorCode.SHADOW_IS_WRITING;
        }
        rwLock.writeLock().lock();
        try {
            // 清空desire
            doc.getState().setDesired(null);
            if (null != doc.getMetadata()) {
                doc.getMetadata().getDesired().clear();
                doc.getMetadata().getDesired().put(ShadowConst.DOC_KEY_TIMESTAMP, System.currentTimeMillis());
            }
            return null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * @Description 由服务器端影子更新文档
     * @param timestamp 时间戳
     * @return 是否出错
     * @author szh
     * @Date 2019/5/2 17:28
     */
    public ReErrorCode updateShadowByServer(long timestamp) {
        // 判断是否加了写锁
        if (rwLock.isWriteLocked()) {
            return ReErrorCode.SHADOW_IS_WRITING;
        }
        rwLock.writeLock().lock();
        try {
            // 对照list的变更
            List<String> entityNames = ClassUtils.getAllEntityName();
            compareAttr(data, (ShadowEntity) doc.getState().getReported(data.getClass()), entityNames);
            // 回退影子对象，当设备端修改成功之后才更改影子对象
            shadowRevert();
            // 更新文档
            // desired
            for (ShadowField sf : shadowField.values()) {
                switch (sf.getOperation()) {
                    case ADD:
                        doc.getState().getDesired().getAdd().add(sf);
                        break;

                    case DELETE:
                        doc.getState().getDesired().getDelete().add(sf.getSri());
                        break;

                    case UPDATE:
                        doc.getState().getDesired().getUpdate().add(sf);
                        break;
                }
            }
            // metadata
            for (String sri : shadowField.keySet()) {
                Map<String, Object> metadataTime = new HashMap<>();
                metadataTime.put(ShadowConst.DOC_KEY_TIMESTAMP, timestamp);
                doc.getMetadata().getDesired().put(sri, metadataTime);
            }

            // 更新时间戳
            doc.setTimestamp(timestamp);
            // 更新版本
            doc.addUpVersion();

            return null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * @Description 影子版本回退
     * @author szh
     * @Date 2019/4/30 17:15
     */
    private void shadowRevert() {
        // TODO 影子版本回退 我不会写了 基本类型可以覆盖，对象不能
        // 需要针对增、删、改进行不同的回退，对象还要考虑容器注入问题
        ShadowEntity shadowAttr = (ShadowEntity) doc.getState().getReported(data.getClass());
//        // 使用影子文档部分的数据覆盖影子对象
//        for (String key : shadowAttr.keySet()) {
//            ClassUtils.setValue(data, key, shadowAttr.get(key));
//        }
    }

    /**
     * @Description 对比属性不同
     * @return 不同的属性名及其值
     * @author szh
     * @Date 2019/5/2 17:11
     */
//    private Map<String, Object> compareAttr() {
//        Map<String, Object> diffAttr = new HashMap<>(); // 不同的属性
//        Map<String, Object> attrDoc = doc.getState().getReported(); // 属性文档
//
//        try {
//            Class dataClass = data.getClass();
//            Field[] fields = dataClass.getDeclaredFields();
//            // 遍历影子所有属性
//            for (Field f : fields) {
//                f.setAccessible(true);
//                // 获取影子值并转换为JSON
//                String fieldName = f.getName();
//                Object attrValue = f.get(data);
//                String attrJson = JSONObject.toJSONString(attrValue);
//                // 获取文档中属性并转换JSON
//                boolean isDiff;
//                Object docValue = attrDoc.get(fieldName);
//                isDiff = docValue == null;
//                if (null != docValue) {
//                    String docJson = JSONObject.toJSONString(docValue);
//                    // 对比文档属性异同
//                    isDiff = !docJson.equals(attrJson);
//                }
//                if (isDiff) {
//                    diffAttr.put(fieldName, attrValue);
//                }
//            }
//        } catch (Exception e) {
//            log.error("compareAttr error: " + e.getMessage());
//            return new HashMap<>();
//        }
//
//        return diffAttr;
//    }

    /**
     * @Description 比较实体的list
     * @param entity 实体
     * @param doc 影子文档
     * @param entityNames 实体name
     * @author szh
     * @Date 2019/6/26 19:01
     */
    private void compareAttr(ShadowEntity entity, ShadowEntity doc, List<String> entityNames) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            String fieldType = f.getType().getSimpleName();
            String fieldName = f.getName();
            if ("List".equals(fieldType)) {
                List<ShadowEntity> newList = (List<ShadowEntity>) ClassUtils.getValue(entity, fieldName);
                List<ShadowEntity> oldList = (List<ShadowEntity>) ClassUtils.getValue(doc, fieldName);
                compareList(newList, oldList, entity.getSRI());
            } else if (entityNames.contains(fieldType)) {
                ShadowEntity childEntity = (ShadowEntity) ClassUtils.getValue(entity, fieldName);
                ShadowEntity childDoc = (ShadowEntity) ClassUtils.getValue(doc, fieldName);
                compareAttr(childEntity, childDoc, entityNames);
            }
        }
    }

    /**
     * @Description 对比list变化
     * @param newList 新list
     * @param oldList 原list
     * @param parentSri 父级sri
     * @author szh
     * @Date 2019/6/26 17:07
     */
    private void compareList(List<ShadowEntity> newList, List<ShadowEntity> oldList, String parentSri) {
        if ((null == oldList || oldList.isEmpty()) && (null == newList || newList.isEmpty())) {
            return;
        }

        // 要删除的
        List<ShadowEntity> toDelete = null;
        // 要增加的
        List<ShadowEntity> toAdd = null;

        if ((null == oldList || oldList.isEmpty())) {
            // 原list为空，新list有内容，则全部为新增
            toAdd = newList;
        } else if ((null == newList || newList.isEmpty())) {
            // 新list为空，原list有内容，则全部删除
            toDelete = oldList;
        } else {
            toDelete = oldList.stream().filter(item -> !newList.contains(item)).collect(Collectors.toList());
            toAdd = newList.stream().filter(item -> !oldList.contains(item)).collect(Collectors.toList());
        }

        // 删除
        if (null != toDelete) {
            for (ShadowEntity entity : toDelete) {
                ShadowField delField = new ShadowField(entity.getClass().getSimpleName(), entity.getSRI(), EntityOperation.DELETE);
                shadowField.put(entity.getSRI(), delField);
            }
        }
        // 增加
        if (null != toAdd) {
            for (ShadowEntity entity : toAdd) {
                ShadowField addField = new ShadowField(entity.getClass().getSimpleName(), entity.getSRI(), parentSri,
                        ClassUtils.getValueMap(entity), EntityOperation.ADD);
                shadowField.put(entity.getSRI(), addField);
            }
        }
    }


    /**
     * 读数据，可以多个线程同时读， 所以上读锁即可
     */
    public ShadowEntity get() {
        /* 上读锁 */
        rwLock.readLock().lock();
        try {
            return this.data;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rwLock.readLock().unlock();
        }
        return null;
    }


    /**
     * 写数据，多个线程不能同时 写 所以必须上写锁
     *
     * @param data
     */
    public void put(ShadowEntity data) {
        /* 上写锁 */
        rwLock.writeLock().lock();
        try {
            //System.out.println(Thread.currentThread().getName() + " 准备写数据!");
            /* 休眠 */
            TimeUnit.SECONDS.sleep(5);
            this.data = data;
            //System.out.println(Thread.currentThread().getName() + " 写入的数据: " + data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

}
