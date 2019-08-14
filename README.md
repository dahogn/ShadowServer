# ShadowServer2
### 一、概述
目前物联网平台中经常遇到设备与后台之间的数据同步问题，程序员需要编写复杂的逻辑检查设备与数据库之间的数据一致性。
本项目将实际的物理IoT设备映射成为内存对象，并对这些对象进行管理，为开发者提供内存对象与数据库，
内存对象与物理设备之间的自动同步机制，隐藏数据库与内存模型之间的交互环节，让开发者专注于业务逻辑开发。
### 二、使用流程
#### 1. 物理实体构建
开发者提供xml文件定义实体的结构，这些实体交由影子系统管理，并在用户需要时取出操作。  
xml类定义示例：
```xml
<class name="CargoRoad">
    <id type="int" table="cargo_road" column="id">id</id>
    <field type="Integer" table="cargo_road" column="serial">serial</field>
    <list type="Commodity" table="re_road_commodity">commodity</list>
</class>
```
#### 2. 数据库映射
以用户提供的实体为模板，在数据库中新建对应的数据表，服务启动时自动扫描数据库实体并载入内存中，影子实体均为 ***ShadowEntity*** 子类，
项目中在不确定使用的实体类型时，多返回ShadowEntity
#### 3.影子使用
在使用影子的service方法上需要添加 ***@ShadowService*** 注解，此注解提供在方法抛出异常后内存对象的数据回滚功能，并使影子是线程安全的，
影子的获取和修改后的提交在ShadowUtils类中提供
##### I. 影子获取
1. 通过用户指定的topic获取对应实体
```java
Vending vending = (Vending) ShadowUtils.getShadow("vending");
```
2. 获得一个类型的所有影子
```java
List<ShadowEntity> vendingList = ShadowUtils.getShadowList(Vending.class);
```
##### II. 影子操作
新建一个实体时必须指定这个实体所属于的设备的通信topic
```java
Commodity commodity = new Commodity("vending");
```
##### III. 影子提交
修改完影子相关属性后将修改提交暂存到内存中，通过topic指定要提交的设备实体，提交时会检查每个实体的SRI，如果缺少会抛出NoSriException异常
```java
try {
    ShadowUtils.commit("vending");
} catch (Exception e) {
    log.error(e.getMessage);
}
```
##### IV. 影子推送
将暂存的修改推送到设备端，通过topic指定要推送的设备实体
```java
ShadowUtils.push("vending");
```
也可以在提交修改的同时立即推送到设备，同样会检查每个实体的SRI
```java
try {
    ShadowUtils.commitAndPush("vending");
} catch (Exception e) {
    log.error(e.getMessage);
}
```
此时内存中的影子会回滚到未修改的状态，也就是与设备端上报的数据一致，直到设备端上报修改数据，才更新内存中的影子以及修改数据库中的数据
##### V. 影子持久化
手动将内存中的实体数据持久化到数据库中，同样通过topic指定持久化的影子
```java
ShadowUtils.save(String topic);
```
##### VI. 新增影子对象到框架进行管理
当服务器端新增设备时，可以手动将设备实体注入到框架进行管理，此方法会检查设备对象以及每一个受管理的子实体的topic及SRI，如果缺少则抛出
NoTopicException和NoSriException异常
```java
try {
    String topic = "vending_topic";
    Vending vending = new Vending(topic);
    ShadowUtils.addShadow(vending, topic);
} catch (Exception e) {
    log.error(e.getMessage);
}
```
##### VII. 直接获得影子的某个子实体
通过实体的SRI可以获得
```java
Commodity commodity = (Commodity) ShadowUtils.getEntity("Commodity_1234567891011_001");
```
##### VIII. 新建实体手动交由框架管理
新建实体调用带有topic入参的构造函数，此时实体会自动在框架中完成注册接受管理，新建的实体后必须在框架中注册才能检测到实体后续发生的变化，
实现自动同步，但有时会使用默认无参的构造函数新建临时实体，后续需要进行注册时调用此方法，方法同样检测topic及SRI
```java
try {
    Vending vending = (Vending) ShadowUtils.getShadow("vending");
    Commodity commodity = new Commodity();
    commodity.setEntityTopic("vending");
    vending.getCommodiyList.add(commodity);
    ShadowUtils.addEntity(commodity);
} catch (Exception e) {
    log.error(e.getMessage);
}
```
##### IX. 手动数据回滚
用户手动处理异常需要回滚数据时调用此方法，通过topic指定需要回滚的影子，数据会回滚到设备最后上报的状态，与设备上的数据保持一致，
回滚时检查SRI
```java
try {
    ShadowUtils.revert("vending");
} catch (Exception e) {
    log.error(e.getMessage);
}
```