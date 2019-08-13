import React, { Component } from "react";
import PageHeaderLayout from '../../layouts/PageHeaderLayout';
import { Table, Card } from "antd";
import styles from '../../common/common.less';
import { connect } from 'dva';
import CommodityModal from './CommodityModal';

@connect(({ cargoRoad }) => ({ cargoRoad }))
export default class CargoRoad extends Component {

  componentWillMount() {
    const { dispatch, cargoRoad: { vending: { sri } } } = this.props;
    dispatch({
      type: 'cargoRoad/getCargoRoadList',
      payload: sri,
    });
  }

  callbackRefresh = () => {

  };

  handleCommodityVisible = data => {
    const { dispatch } = this.props;
    dispatch({
      type: 'cargoRoad/setCommodityModalVisible',
      payload: data,
    });
  };

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

  handleCloseCommodityModal = visible => {
    const { dispatch } = this.props;
    dispatch({
      type: 'cargoRoad/setCommodityList',
      payload: [],
    });
    this.handleCommodityVisible(visible);
  };

  handleEditCommodity = record => {
    const { dispatch } = this.props;
  };

  handleEditCommodityCallback = () => {
    this.handleCloseCommodityModal(false);
    this.callbackRefresh();
  };

  render() {

    const { cargoRoad: { cargoRoadList, commodityModalVisible } } = this.props;

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
      </PageHeaderLayout>
    )

  }

}
