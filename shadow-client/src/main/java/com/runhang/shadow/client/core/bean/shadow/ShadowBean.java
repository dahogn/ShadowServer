package com.runhang.shadow.client.core.bean.shadow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.runhang.shadow.client.common.utils.BeanUtils;
import com.runhang.shadow.client.common.utils.ClassUtils;
import com.runhang.shadow.client.core.bean.comm.ShadowConst;
import com.runhang.shadow.client.core.enums.EntityOperation;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.exception.NoSriException;
import com.runhang.shadow.client.core.exception.NoTopicException;
import com.runhang.shadow.client.core.model.ShadowField;
import com.runhang.shadow.client.core.shadow.EntityFactory;
import com.runhang.shadow.client.core.sync.database.DatabaseQueue;
import com.runhang.shadow.client.device.entity.ShadowEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
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
     * key: sri
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
            // 更新值
            Map<String, Object> f = shadowField.get(field.getSri()).getField();
            f.putAll(field.getField());
            // 原有值
            Map<String, Object> o = shadowField.get(field.getSri()).getOriginalField();
            o.putAll(field.getOriginalField());
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
    public ReErrorCode updateShadowByDevice(ShadowDesiredDoc updateValue) throws NoSriException, NoTopicException {
        // 判断是否加了写锁
        if (rwLock.isWriteLocked()) {
            return ReErrorCode.SHADOW_IS_WRITING;
        }
        rwLock.writeLock().lock();
        try {
            /** step1: 更新影子属性 **/
            // metadata
            long current = System.currentTimeMillis();
            ShadowDocMetadata metadata = doc.getMetadata();
            if (null == metadata) {
                metadata = new ShadowDocMetadata();
                doc.setMetadata(metadata);
            }

            // 增加
            for (ShadowField addField : updateValue.getAdd()) {
                if (null != addField.getParentSri() && EntityFactory.isSriExist(addField.getParentSri())) {
                    // 获取父类实体
                    ShadowEntity parentEntity = EntityFactory.getEntity(addField.getParentSri());
                    // 实体补充sri和topic
                    Map<String, Object> field = addField.getField();
                    field.put("SRI", addField.getSri());
                    field.put("entityTopic", topic);
                    // 实体属性反序列化，强转成为用户定义实体
                    int disableDecimalFeature = JSON.DEFAULT_PARSER_FEATURE & ~Feature.UseBigDecimal.getMask();
                    String json = JSONObject.toJSONString(addField.getField());
                    Class<?> entityClass = Class.forName(ClassUtils.getEntityPackageName(addField.getClassName()));
                    ShadowEntity entity = JSONObject.parseObject(json, entityClass, disableDecimalFeature);
                    // 影子更新
                    if (null != entity) {
                        // data
                        List<String> entityNames = ClassUtils.getAllEntityName();
                        EntityFactory.injectEntities(entity, topic, entityNames);
                        ClassUtils.listAdd(parentEntity, addField.getFieldName(), entity);
                        // metadata
                        Map<String, Object> metadataTime = new HashMap<>();
                        metadataTime.put(ShadowConst.DOC_KEY_TIMESTAMP, current);
                        metadata.getReported().put(addField.getParentSri(), metadataTime);
                    }
                }
            }
            // 删除
            for (ShadowField delField : updateValue.getDelete()) {
                if (null != delField.getSri() && EntityFactory.isSriExist(delField.getSri()) &&
                        null != delField.getParentSri() && EntityFactory.isSriExist(delField.getParentSri())) {
                    // data
                    ShadowEntity parentEntity = EntityFactory.getEntity(delField.getParentSri());
                    ShadowEntity delEntity = EntityFactory.getEntity(delField.getSri());
                    ClassUtils.listRemove(parentEntity, delField.getFieldName(), delEntity);
                    // metadata
                    Map<String, Object> metadataTime = new HashMap<>();
                    metadataTime.put(ShadowConst.DOC_KEY_TIMESTAMP, current);
                    metadata.getReported().put(delField.getParentSri(), metadataTime);
                }
            }
            // 更新
            for (ShadowField updateField : updateValue.getUpdate()) {
                if (null != updateField.getSri() && EntityFactory.isSriExist(updateField.getSri())) {
                    ShadowEntity entity = EntityFactory.getEntity(updateField.getSri());
                    if (null != entity) {
                        for (String fieldName : updateField.getField().keySet()) {
                            boolean updateSuccess = ClassUtils.setValue(entity, fieldName, updateField.getField().get(fieldName));
                            if (!updateSuccess) {
                                // 影子属性回退
                                shadowRevert();
                                return ReErrorCode.SHADOW_ATTR_WRONG;
                            } else {
                                // metadata
                                Map<String, Object> metadataTime = new HashMap<>();
                                metadataTime.put(ShadowConst.DOC_KEY_TIMESTAMP, current);
                                metadata.getReported().put(updateField.getSri(), metadataTime);
                            }
                        }
                    }
                }
            }

            /** step2 更新数据库 **/
            DatabaseQueue.amqpSave(data);

            /** step3 更新文档属性 **/
            // 序列化保存状态
            doc.getState().setReported(data);
            // 更新时间戳
            doc.setTimestamp(current);

            return null;
        } catch (NoTopicException | NoSriException e) {
            throw e;
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
    public ReErrorCode clearDesired() {
        // 判断是否加了写锁
        if (rwLock.isWriteLocked()) {
            return ReErrorCode.SHADOW_IS_WRITING;
        }
        rwLock.writeLock().lock();
        try {
            // 清空desire
            doc.getState().getDesired().clearDesired();
            // 清空变更
            shadowField.clear();
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
    public ReErrorCode updateShadowByServer(long timestamp) throws NoSriException {
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
                        doc.getState().getDesired().getDelete().add(sf);
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
        for (ShadowField sf : shadowField.values()) {
            switch (sf.getOperation()) {
                case ADD:
                    // 新增的删掉
                    Object addParentObj = BeanUtils.getBean(sf.getParentSri());
                    Object addObj = BeanUtils.getBean(sf.getSri());
                    if (null != addParentObj && null != addObj) {
                        ClassUtils.listRemove(addParentObj, sf.getFieldName(), addObj);
                    }
                    break;

                case DELETE:
                    // 删除的加回来
                    Object delParentObj = BeanUtils.getBean(sf.getParentSri());
                    Object delObj = BeanUtils.getBean(sf.getSri());
                    if (null != delParentObj && null != delObj) {
                        ClassUtils.listRemove(delParentObj, sf.getFieldName(), delObj);
                    }
                    break;

                case UPDATE:
                    // 更新的恢复
                    Object updateObj = BeanUtils.getBean(sf.getSri());
                    if (null != updateObj) {
                        for (String updateField : sf.getOriginalField().keySet()) {
                            ClassUtils.setValue(updateObj, updateField, sf.getOriginalField().get(updateField));
                        }
                    }
                    break;
            }
        }
    }

    /**
     * @Description 比较实体的list
     * @param entity 实体
     * @param doc 影子文档
     * @param entityNames 实体name
     * @author szh
     * @Date 2019/6/26 19:01
     */
    private void compareAttr(ShadowEntity entity, ShadowEntity doc, List<String> entityNames) throws NoSriException {
        // 检查实体sri
        if (StringUtils.isEmpty(entity.getSRI())) {
            throw new NoSriException();
        }

        // 遍历对比
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            String fieldType = f.getType().getSimpleName();
            String fieldName = f.getName();
            if ("List".equals(fieldType)) {
                // 是list属性则比照内存中的对象与影子文档中的不同
                List<ShadowEntity> newList = (List<ShadowEntity>) ClassUtils.getValue(entity, fieldName);
                List<ShadowEntity> oldList = (List<ShadowEntity>) ClassUtils.getValue(doc, fieldName);
                compareList(newList, oldList, entity.getSRI(), fieldName, entityNames);
            } else if (entityNames.contains(fieldType)) {
                // 是受管理的实体则递归遍历继续比照内部的list
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
    private void compareList(List<ShadowEntity> newList, List<ShadowEntity> oldList, String parentSri,
                             String listName, List<String> entityNames) throws NoSriException {
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
                ShadowField delField = new ShadowField(entity.getClass().getSimpleName(), entity.getSRI(), listName,
                        parentSri, EntityOperation.DELETE);
                shadowField.put(entity.getSRI(), delField);
            }
        }
        // 增加
        if (null != toAdd) {
            for (ShadowEntity entity : toAdd) {
                ShadowField addField = new ShadowField(entity.getClass().getSimpleName(), entity.getSRI(), listName,
                        parentSri, ClassUtils.getValueMap(entity, Collections.singletonList("databaseFieldMap")), EntityOperation.ADD);
                shadowField.put(entity.getSRI(), addField);
            }
        }

        // 继续比较list中没有删除和修改的部分的实体变化
        if (null != newList && null != oldList) {
            // 获得相同部分
            List<ShadowEntity> newListSame = newList.stream().filter(oldList::contains).collect(Collectors.toList());
            List<ShadowEntity> oldListSame = oldList.stream().filter(newList::contains).collect(Collectors.toList());

            for (ShadowEntity memEntity : newListSame) {
                // 获取相同的实体
                Optional<ShadowEntity> docEntityOp = oldListSame.stream().filter(item -> item.equals(memEntity)).findFirst();
                if (docEntityOp.isPresent()) {
                    ShadowEntity docEntity = docEntityOp.get();
                    compareAttr(memEntity, docEntity, entityNames);
                }
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
