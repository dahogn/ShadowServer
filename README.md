# ShadowServer2
### 一、概述
项目将实际的物理IoT设备映射成为内存对象，并对这些对象进行管理，为开发者提供内存对象与数据库，内存对象与物理设备之间的自动同步机制，让开发者专注于业务逻辑开发。
### 二、使用流程
#### 1. 物理实体构建
开发者提供xml文件定义实体的结构，这些实体交由影子系统管理，并在用户需要时取出操作。
#### 2. 数据库映射
以用户提供的实体为模板，在数据库中新建对应的数据表，服务启动时自动扫描数据库实体并载入内存中
#### 3.影子使用
影子的获取和修改后的提交在ShadowUtils类中提供
##### I. 影子获取
通过用户指定的topic获取对应实体
```
Vending vending = (Vending) ShadowUtils.getShadow("vending");
```
##### II. 影子操作
新建一个实体时必须指定这个实体所属于的设备的通信topic
```
Commodity commodity = new Commodity("vending");
```
##### III. 影子提交
修改完影子相关属性后将修改提交暂存到内存中，通过topic指定要提交的设备实体
```
ShadowUtils.commit("vending");
```
##### IV. 影子推送
将暂存的修改推送到设备端，通过topic指定要推送的设备实体
```
ShadowUtils.push("vending");
```
也可以在提交修改的同时立即推送到设备
```
ShadowUtils.commitAndPush("vending");
```