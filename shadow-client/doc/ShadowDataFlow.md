# 影子数据流
### 一、数据结构
bean.shadow包中为内存影子类组成，bean.comm包中为通信使用的bean
![类图](https://github.com/shengzh10/ShadowServer2/raw/master/shadow-client/doc/Class%20Diagram.jpg "影子类图")
### 二、影子管理
#### 1. 影子结构
接受框架管理的主要是core.shadow中的ShadowBean类，这是对交给用户操作的ShadowEntity对象的包装，这个类主要由影子对象、
影子文档和变化属性三部分组成：
   * 影子对象（data）：最终交给开发者的实体对象
   * 影子文档（doc）：序列化的影子对象以及时间戳等信息，数据保持与设备端最后一次的上报一致
   * 变化属性（shadowField）：记录开发者在使用过程中对影子对象属性的增删改操作，在下发到设备的时候使用其中的数据
#### 2. 实体管理
框架管理的是包装过的ShadowBean对象，以及组成设备对象的内嵌实体对象，这些实体使用唯一的影子资源标识符（SRI, Shadow Resource Identifier）
进行标识管理
#### 3. 数据变化监测
针对实体中的普通使用setter改变的属性以及list属性分别采用不同的方法对发生变化的属性进行记录：  
对于普通属性，采用观察者模式，记录改变的属性名，改变前的值，改变后的值  
对于list属性，在提交的时候对比影子对象以及反序列化的影子文档，记录增加和删除的对象
#### 4. 影子数据事务性
使用信号量对ShadowBean对象加锁，同一时刻只能有一个线程操作这个影子，在开发者使用影子的函数上添加 ***@ShadowService*** 注解，
这样在业务逻辑结束后可以自动释放使用的影子的锁。同时可以在开发者的逻辑抛出异常的时候使用反序列化的影子文档对影子对象进行回滚
### 三、应用程序更改设备状态
开发者在修改完设备影子信息之后调用ShadowFactory的commitAntPush方法提交更改并推送期望状态到设备。
1. 以在售货机中增加一个货道为例，commit提交更改后内存中影子文档为
```json
{
    "metadata":{
        "desired":{
            "CargoRoad_1560758107239_367":{
                "timestamp":1561603988536
            }
        },
        "reported":{}
    },
    "state":{
        "desired":{
            "add":[
                {
                    "className":"CargoRoad",
                    "sri":"CargoRoad_1560758107239_367",
                    "parentSri":"Vending_1560758107221_553",
                    "field":{
                        "serial":1,
                        "commodity":[
                            {
                                "entityTopic":"vending",
                                "id":1,
                                "name":"可乐",
                                "number":1,
                                "price":1.1,
                                "sri":"Commodity_1560758106907_511"
                            }
                        ]
                    }
                }
            ],
            "delete":[],
            "update":[]
        },
        "reported":{
        }
    },
    "timestamp":1561603988536,
    "version":1
}
```
2. 影子更新完成后发送到 get/${deviceTopic} 主题中  
desired为服务器端对设备端的期望数据，其中add为list增加实体，update为对实体属性的更新，delete为删除list中的实体，下面是以add为例的字段含义

字段|字段名称|字段含义
:---|:---|:---
className|实体类名|增加的实体类名
sri|影子资源标识符|系统自动生成的实体标识符
parentSri|父级影子资源标识符|要修改的属性所属的父级实体的实体标识符
fieldName|属性名称|要修改的属性在所属父级实体中的属性名称
field|实体属性|增加的实体所有的属性值

```json
{
    "method": "control",
    "payload": {
        "status": "success",
        "state": {
            "desired":{
                "add":[
                    {
                        "className":"CargoRoad",
                        "sri":"CargoRoad_1560758107239_367",
                        "parentSri":"Vending_1560758107221_553",
                        "fieldName":"cargoRoad",
                        "field":{
                            "serial":1,
                            "commodity":[
                                {
                                    "entityTopic":"vending",
                                    "id":1,
                                    "name":"可乐",
                                    "number":1,
                                    "price":1.1,
                                    "sri":"Commodity_1560758106907_511"
                                }
                            ]
                        }
                    }
                ],
                "delete":[],
                "update":[]
            }
        },
        "metadata":{
            "desired":{
                "CargoRoad_1560758107239_367":{
                    "timestamp":1561603988536
                }
            },
            "reported":{
            }
        }
    },
    "version": 2,
    "timestamp": 1561603988536
}
```
3. 设备端更新成功之后，上报最新状态到平台 update/${deviceTopic}
```json
{
    "method":"update",
    "state":{
        "reported":{
            "add":[
                {
                    "className":"CargoRoad",
                    "sri":"CargoRoad_1560758107239_367",
                    "parentSri":"Vending_1560758107221_553",
                    "field":{
                        "serial":1,
                        "commodity":[
                            {
                                "entityTopic":"vending",
                                "id":1,
                                "name":"可乐",
                                "number":1,
                                "price":1.1,
                                "sri":"Commodity_1560758106907_511"
                            }
                        ]
                    }
                }
            ],
            "delete":[],
            "update":[]
        },
        "desired":null
    },
    "version":3
}
```
### 四、设备主动上报状态
1. 设备发送状态到影子服务器 update/${deviceTopic}  
desired为null的时候表示设备端更新数据成功，清空服务器端的影子desired数据，为保证设备端与数据库中sri一致性，新增的实体的sri需要设备端自动生成
```json
{
    "method":"update",
    "state":{
        "reported":{
            "update":[
                {
                    "className":"Vending",
                    "sri":"Vending_1560758107221_553",
                    "parentSri":"Vending_1560758107221_553",
                    "field":{
                        "name":"vending4"
                    }
                }
            ],
            "delete":[
                {
                    "className":"Commodity",
                    "sri":"Commodity_1560758106907_511",
                    "parentSri":"CargoRoad_1560758107239_367",
                    "fieldName":"commodity"
                }
            ],
            "add":[
                {
                    "className":"Commodity",
                    "sri":"Commodity_1560758106907_222",
                    "parentSri":"CargoRoad_1560758107239_367",
                    "fieldName":"commodity",
                    "field":{
                        "number":2,
                        "price":2.2,
                        "name":"coffee"
                    }
                }
            ]
        },
        "desired":null
    },
    "version":4
}
```

2. 影子更新之后发送结果到设备  
若更新成功
```json
{
    "method":"reply",
    "payload":{
        "status":"success",
        "version":4
    },
    "timestamp":1469564576
}
```
更新失败
```json
{
    "method":"reply",
    "payload":{
        "status":"error",
        "content":{
            "errorCode":"${errorCode}",
            "errorMsg":"${errorMsg}"
        }
    },
    "timestamp":1469564576
}
```
错误码说明

错误码|含义
:---:|:---
400|不正确的JSON格式
401|影子JSON缺少method信息
402|影子JSON缺少state字段
403|影子JSON version不是数字
404|影子JSON缺少reported字段
405|影子JSON reported属性字段为空
406|影子JSON method是无效的方法
407|影子内容为空
408|影子reported属性个数超过128个
409|影子版本冲突
500|影子属性不存在
501|影子正在写入
502|影子属性未修改
503|服务端处理异常

### 五、设备主动获取影子内容
1. 设备发送请求到 update/${deviceTopic} 获取影子中的最新状态
```json
{
    "method": "get"
}
```
2. 服务器端下发影子状态到 get/${deviceTopic}
```json
{
    "method":"reply",
    "payload":{
        "metadata":{
            "desired":{
                "Vending_1560758107221_553":{
                    "timestamp":1561965731144
                }
            },
            "reported":{}
        },
        "state":{
            "desired":{
                "add":[],
                "delete":[],
                "update":[
                    {
                        "className":"Vending",
                        "field":{
                            "name":"vending3"
                        },
                        "sri":"Vending_1560758107221_553"
                    }
                ]
            },
            "reported":{
                "cargoRoad":[
                    {
                        "commodity":[
                            {
                                "id":1,
                                "name":"可乐",
                                "number":1,
                                "price":1.1,
                                "sri":"Commodity_1560758106907_511"
                            }
                        ],
                        "id":1,
                        "serial":1,
                        "sri":"CargoRoad_1560758107239_367"
                    }
                ],
                "id":1,
                "name":"vending2",
                "sri":"Vending_1560758107221_553",
                "topic":"vending"
            }
        },
        "status":"success",
        "version":0
    },
    "timestamp":1561969659878,
    "version":4
}
```