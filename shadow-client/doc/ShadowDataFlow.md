# 影子数据流
### 一、影子结构
![类图](https://github.com/shengzh10/ShadowServer2/raw/master/shadow-client/doc/Class%20Diagram.jpg "影子类图")
### 二、应用程序更改设备状态
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
                    "parent":"Vending_1560758107221_553",
                    "field":{
                        "serial":1,
                        "commodity":[
                            {
                                "entityTopic":"vending",
                                "id":1,
                                "name":"可乐",
                                "number":1,
                                "price":1.1,
                                "sRI":"Commodity_1560758106907_511"
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
                        "parent":"Vending_1560758107221_553",
                        "field":{
                            "serial":1,
                            "commodity":[
                                {
                                    "entityTopic":"vending",
                                    "id":1,
                                    "name":"可乐",
                                    "number":1,
                                    "price":1.1,
                                    "sRI":"Commodity_1560758106907_511"
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
                    "parent":"Vending_1560758107221_553",
                    "field":{
                        "serial":1,
                        "commodity":[
                            {
                                "entityTopic":"vending",
                                "id":1,
                                "name":"可乐",
                                "number":1,
                                "price":1.1,
                                "sRI":"Commodity_1560758106907_511"
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
### 三、设备主动上报状态
1. 设备发送状态到影子服务器 update/${deviceTopic}

```json
{
    "method":"update",
    "state":{
        "reported":{
            "update":[
                {
                    "className":"Commodity",
                    "sri":"Commodity_1560758106907_511",
                    "parent":"CargoRoad_1560758107239_367",
                    "field":{
                        "number":3
                    }
                }
            ],
            "delete":[],
            "add":[]
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
:---:|:---:
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

### 四、设备主动获取影子内容
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
    "version":1
}
```