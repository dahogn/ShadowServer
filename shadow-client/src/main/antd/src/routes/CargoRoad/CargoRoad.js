import React, { Component } from "react";
import PageHeaderLayout from '../../layouts/PageHeaderLayout';
import { Table, Card, Divider, Button, Row, Col } from "antd";
import styles from '../../common/common.less';
import { connect } from 'dva';
import CommodityModal from './CommodityModal';
import AddCargoRoadModal from "./AddCargoRoadModal";

@connect(({ cargoRoad }) => ({ cargoRoad }))
export default class CargoRoad extends Component {

  componentWillMount() {
    const { dispatch, cargoRoad: { vending: { sri } } } = this.props;
    dispatch({
      type: 'cargoRoad/getCargoRoadList',
      payload: sri,
    });
  }

  // 回调刷新列表
  callbackRefresh = () => {
    const { dispatch, cargoRoad: { vending: { sri } } } = this.props;
    dispatch({
      type: 'cargoRoad/getCargoRoadList',
      payload: sri,
    });
  };

  // 商品窗口
  handleCommodityVisible = data => {
    const { dispatch } = this.props;
    dispatch({
      type: 'cargoRoad/setCommodityModalVisible',
      payload: data,
    });
  };

  // 点击查看货道中的商品
  handleClickView = (record, visible) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'cargoRoad/setCargoRoadId',
      payload: record.sri,
    });
    dispatch({
      type: 'cargoRoad/fetchCommodity',
      payload: record.sri,
    });
    this.handleCommodityVisible(visible);
  };

  // 关闭商品窗口
  handleCloseCommodityModal = visible => {
    const { dispatch } = this.props;
    dispatch({
      type: 'cargoRoad/setCommodityList',
      payload: [],
    });
    this.handleCommodityVisible(visible);
  };

  // 修改商品
  handleEditCommodity = record => {
    const { dispatch } = this.props;
  };

  // 修改商品回调
  handleEditCommodityCallback = () => {
    this.handleCloseCommodityModal(false);
    this.callbackRefresh();
  };

  // 新增货道窗口
  handleAddModalVisible = visible => {
    const { dispatch } = this.props;
    dispatch({
      type: 'cargoRoad/setAddCargoRoadVisible',
      payload: visible,
    });
  };

  // 新增货道
  handleAddCargoRoad = record => {
    const { dispatch } = this.props;
    dispatch({
      type: 'cargoRoad/addCargoRoad',
      payload: record,
      callback: this.handleAddCargoRoadCallback,
    })
  };

  // 新增货道回调
  handleAddCargoRoadCallback = () => {
    this.handleAddModalVisible(false);
    this.callbackRefresh();
  };

  render() {

    const { cargoRoad: { cargoRoadList, commodityModalVisible, addCargoRoadVisible } } = this.props;

    const columns = [
      {
        title: '序号',
        dataIndex: 'serial',
        key: 'serial',
      },
      {
        title: 'SRI',
        dataIndex: 'sri',
        key: 'sri',
      },
      {
        title: '商品数量',
        dataIndex: 'commodityNum',
        key: 'commodityNum',
      },
      {
        title: '操作',
        dataIndex: 'operation',
        key: 'operation',
        fixed: 'right',
        width: 150,
        render: (text, record) => (
          <a type="dashed" onClick={() => this.handleClickView(record, true)}>查看商品</a>
        )
      },
    ];

    return (
      <PageHeaderLayout title="货道管理">
        <Card bordered={ false }>
          <div className={styles.tableList}>
            <Row gutter={{ md: 6, lg: 24, xl: 48 }}>
              <Col md={3} sm={48}>
                <Button icon="plus" type="primary" onClick={() => this.handleAddModalVisible(true)}>添加货道</Button>
              </Col>
              <Col md={3} sm={48}>
                <Button icon="reload" onClick={() => this.callbackRefresh()}>刷新</Button>
              </Col>
            </Row>

            <Divider style={{ marginBottom: 32 }} />

            <Table
              rowKey={ record => record.sri }
              dataSource={ cargoRoadList }
              columns={ columns }
              pagination={ false }
            />
          </div>
        </Card>

        <CommodityModal
          modalVisible={ commodityModalVisible }
          handleModalVisible={ this.handleCloseCommodityModal }
          onSubmit={ this.handleEditCommodity }
        />

        <AddCargoRoadModal
          modalVisible={ addCargoRoadVisible }
          handleModalVisible={ this.handleAddModalVisible }
          onSubmit={ this.handleAddCargoRoad }
        />
      </PageHeaderLayout>
    )

  }

}
