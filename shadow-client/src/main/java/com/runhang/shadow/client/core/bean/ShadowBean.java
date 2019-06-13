package com.runhang.shadow.client.core.bean;

import com.alibaba.fastjson.JSONObject;
import com.runhang.shadow.client.common.utils.ClassUtils;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName ShadowBean
 * @Description 影子管理对象
 * @Date 2019/4/28 20:39
 * @author szh
 **/
@Slf4j
public class ShadowBean<T> {

    /** 影子读写锁 **/
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    /** 影子主题 **/
    private String topic;
    /** 影子对象 **/
    private T data;
    /** 影子文档 **/
    private ShadowDoc doc;

    public ShadowBean() {
        ShadowDoc doc = new ShadowDoc();
        doc.setState(new ShadowDocState());
        setDoc(doc);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ShadowDoc getDoc() {
        return doc;
    }

    public void setDoc(ShadowDoc doc) {
        this.doc = doc;
    }

    /**
     * @Description 由设备端给定值更新影子
     * @param updateValue 更新属性
     * @return 错误信息
     * @author szh
     * @Date 2019/5/2 15:55
     */
    public ReErrorCode updateShadowByDevice(Map<String, Object> updateValue) {
        // 判断是否加了写锁
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
                ShadowDocData metadata = doc.getMetadata();
                if (null == metadata) {
                    metadata = new ShadowDocData();
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
        }
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
            Map<String, Object> diffAttr = compareAttr();
            if (diffAttr.isEmpty()) {
                // 属性未修改或判断出错
                return ReErrorCode.SHADOW_ATTR_NOT_MODIFIED;
            }
            // 回退影子对象
            shadowRevert();
            // 更新文档
            for (String attr : diffAttr.keySet()) {
                // desire
                doc.getState().getDesired().put(attr, diffAttr.get(attr));
                // metadata
                ShadowDocData metadata = doc.getMetadata();
                if (null == metadata) {
                    metadata = new ShadowDocData();
                }
                Map<String, Object> metadataTime = new HashMap<>();
                metadataTime.put(ShadowConst.DOC_KEY_TIMESTAMP, timestamp);
                metadata.getDesired().put(attr, metadataTime);
                doc.setMetadata(metadata);
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
        Map<String, Object> shadowAttr = doc.getState().getReported();
        // 使用影子文档部分的数据覆盖影子对象
        for (String key : shadowAttr.keySet()) {
            ClassUtils.setValue(data, key, shadowAttr.get(key));
        }
    }

    /**
     * @Description 对比属性不同
     * @return 不同的属性名及其值
     * @author szh
     * @Date 2019/5/2 17:11
     */
    private Map<String, Object> compareAttr() {
        Map<String, Object> diffAttr = new HashMap<>(); // 不同的属性
        Map<String, Object> attrDoc = doc.getState().getReported(); // 属性文档

        try {
            Class dataClass = data.getClass();
            Field[] fields = dataClass.getDeclaredFields();
            // 遍历影子所有属性
            for (Field f : fields) {
                f.setAccessible(true);
                // 获取影子值并转换为JSON
                String fieldName = f.getName();
                Object attrValue = f.get(data);
                String attrJson = JSONObject.toJSONString(attrValue);
                // 获取文档中属性并转换JSON
                boolean isDiff;
                Object docValue = attrDoc.get(fieldName);
                isDiff = docValue == null;
                if (null != docValue) {
                    String docJson = JSONObject.toJSONString(docValue);
                    // 对比文档属性异同
                    isDiff = !docJson.equals(attrJson);
                }
                if (isDiff) {
                    diffAttr.put(fieldName, attrValue);
                }
            }
        } catch (Exception e) {
            log.error("compareAttr error: " + e.getMessage());
            return new HashMap<>();
        }

        return diffAttr;
    }

}
