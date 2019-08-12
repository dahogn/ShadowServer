import React, { Component } from "react";
import PageHeaderLayout from '../../layouts/PageHeaderLayout';
import { Table, Card } from "antd";
import styles from '../../common/common.less';
import { connect } from 'dva';

@connect(({ cargoRoad }) => ({ cargoRoad }))
export default class CargoRoad extends Component {

  componentWillMount() {
    const { dispatch, cargoRoad: { vending: { sri } } } = this.props;
    dispatch({
      type: 'cargoRoad/getCargoRoadList',
      payload: sri,
    });
  }

  render() {

    const { cargoRoad: { cargoRoadList } } = this.props;

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
      </PageHeaderLayout>
    )

  }

}
