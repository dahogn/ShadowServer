import React, { PureComponent, Fragment } from 'react';
import { Table, Button, Input, InputNumber, message, Popconfirm, Divider } from 'antd';
import styles from './style.less';

export default class CommodityTableForm extends PureComponent {
  constructor(props) {
    super(props);

    this.state = {
      data: props.value,
      loading: false,
    };
  }
  componentWillReceiveProps(nextProps) {
    if ('value' in nextProps) {
      this.setState({
        data: nextProps.value,
      });
    }
  }
  getRowByKey(key, newData) {
    return (newData || this.state.data).filter(item => item.sri === key)[0];
  }
  index = 0;
  cacheOriginData = {};
  toggleEditable = (e, key) => {
    e.preventDefault();
    const newData = this.state.data.map(item => ({ ...item }));
    const target = this.getRowByKey(key, newData);
    if (target) {
      // 进入编辑状态时保存原始数据
      if (!target.editable) {
        this.cacheOriginData[key] = { ...target };
      }
      target.editable = !target.editable;
      this.setState({ data: newData });
    }
  };
  remove(key) {
    const newData = this.state.data.filter(item => item.sri !== key);
    this.setState({ data: newData });
    this.props.onChange(newData);
  }
  newCommodity = () => {
    const newData = this.state.data.map(item => ({ ...item }));
    let key = parseInt(new Date().valueOf() % 100000000);
    newData.push({
      sri: key,
      name: '',
      num: 0,
      price: 0.0,
      editable: true,
      isNew: true,
    });
    this.index += 1;
    this.setState({ data: newData });
  };
  handleKeyPress(e, key) {
    if (e.key === 'Enter') {
      this.saveRow(e, key);
    }
  }
  handleFieldChange(e, fieldName, key) {
    const newData = this.state.data.map(item => ({ ...item }));
    const target = this.getRowByKey(key, newData);
    if (target) {
      // target[fieldName] = e.target.value;
      target[fieldName] = e;
      this.setState({ data: newData });
    }
  }
  saveRow(e, key) {
    e.persist();
    this.setState({
      loading: true,
    });
    setTimeout(() => {
      if (this.clickedCancel) {
        this.clickedCancel = false;
        return;
      }
      const target = this.getRowByKey(key) || {};
      if (!target.name || !target.num || !target.price) {
        message.error('请填写完整商品信息');
        e.target.focus();
        this.setState({
          loading: false,
        });
        return;
      }
      if (target.num < 0) {
        message.error('商品数量不能小于0');
        e.target.focus();
        this.setState({
          loading: false,
        });
        return;
      }
      if (target.price <= 0) {
        message.error('商品价格必须大于0');
        e.target.focus();
        this.setState({
          loading: false,
        });
        return;
      }

      delete target.isNew;
      this.toggleEditable(e, key);
      this.props.onChange(this.state.data);
      this.setState({
        loading: false,
      });
    }, 500);
  }
  cancel(e, key) {
    this.clickedCancel = true;
    e.preventDefault();
    const newData = this.state.data.map(item => ({ ...item }));
    const target = this.getRowByKey(key, newData);
    if (this.cacheOriginData[key]) {
      Object.assign(target, this.cacheOriginData[key]);
      target.editable = false;
      delete this.cacheOriginData[key];
    }
    this.setState({ data: newData });
    this.clickedCancel = false;
  }
  render() {
    const columns = [
      {
        title: 'SRI',
        dataIndex: 'sri',
        key: 'sri',
        width: '30%',
        align: 'center',
      },
      {
        title: '商品名称',
        dataIndex: 'name',
        key: 'name',
        width: '15%',
        render: (text, record) => {
          if (record.editable) {
            return (
              <Input
                value={text}
                onChange={e => this.handleFieldChange(e.target.value, 'name', record.sri)}
                onKeyPress={e => this.handleKeyPress(e, record.sri)}
                placeholder="商品名称"
              />
            );
          }
          return text;
        },
      },
      {
        title: '商品数量',
        dataIndex: 'num',
        key: 'num',
        width: '15%',
        align: 'center',
        render: (text, record) => {
          if (record.editable) {
            return (
              <InputNumber
                value={text}
                onChange={e => this.handleFieldChange(e, 'num', record.sri)}
                onKeyPress={e => this.handleKeyPress(e, record.sri)}
                placeholder="数量"
                min={0}
                step={1}
                precision={0}
              />
            );
          } else {
            return text;
          }
        },
      },
      {
        title: '商品价格',
        dataIndex: 'price',
        key: 'price',
        width: '15%',
        align: 'center',
        render: (text, record) => {
          if (record.editable) {
            return (
              <InputNumber
                value={text}
                onChange={e => this.handleFieldChange(e, 'price', record.sri)}
                onKeyPress={e => this.handleKeyPress(e, record.sri)}
                placeholder="价格"
                formatter={value => `${value}元`}
                parser={value => value.replace('元', '')}
                min={0}
                step={0.1}
                precision={2}
              />
            );
          } else {
            text = text + '元';
          }
          return text;
        },
      },
      {
        title: '操作',
        key: 'action',
        align: 'center',
        render: (text, record) => {
          if (!!record.editable && this.state.loading) {
            return null;
          }
          if (record.editable) {
            if (record.isNew) {
              return (
                <span>
                  <a onClick={e => this.saveRow(e, record.sri)}>添加</a>
                  <Divider type="vertical" />
                  <Popconfirm title="是否要删除此行？" onConfirm={() => this.remove(record.sri)}>
                    <a>删除</a>
                  </Popconfirm>
                </span>
              );
            }
            return (
              <span>
                <a onClick={e => this.saveRow(e, record.sri)}>保存</a>
                <Divider type="vertical" />
                <a onClick={e => this.cancel(e, record.sri)}>取消</a>
              </span>
            );
          }
          return (
            <span>
              <a onClick={e => this.toggleEditable(e, record.sri)}>编辑</a>
              <Divider type="vertical" />
              <Popconfirm title="是否要删除此行？" onConfirm={() => this.remove(record.sri)}>
                <a>删除</a>
              </Popconfirm>
            </span>
          );
        },
      },
    ];

    return (
      <Fragment>
        <Table
          loading={this.state.loading}
          columns={columns}
          dataSource={this.state.data}
          pagination={false}
          rowClassName={record => {
            return record.editable ? styles.editable : '';
          }}
          rowKey="sri"
        />
        <Button
          style={{ width: '100%', marginTop: 16, marginBottom: 8 }}
          type="dashed"
          onClick={this.newCommodity}
          icon="plus"
        >
          新增商品
        </Button>
      </Fragment>
    );
  }
}
