import React, { Component } from "react";
import PageHeaderLayout from '../../layouts/PageHeaderLayout';
import { Table, Card } from "antd";
import styles from '../../common/common.less';
import { connect } from 'dva';
import { routerRedux } from 'dva/router';

@connect(({ vending, cargoRoad }) => ({ vending, cargoRoad }))
export default class Vending extends Component {

  componentWillMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'vending/getVendingList',
    });
  }

  handleClickVending = (record) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'cargoRoad/setVending',
      payload: record,
    });
    dispatch(routerRedux.push('/cargoRoad'));
  };

  render() {

    const { vending: { vendingList } } = this.props;

    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: '主题',
        dataIndex: 'topic',
        key: 'topic',
      },
      {
        title: 'SRI',
        dataIndex: 'sri',
        key: 'sri',
      },
      {
        title: '货道数量',
        dataIndex: 'cargoRoadNum',
        key: 'cargoRoadNum',
      },
      {
        title: '操作',
        dataIndex: 'operation',
        key: 'operation',
        render: (text, record) => (
            <a type="dashed" onClick={() => this.handleClickVending(record)}>查看货道</a>
        )
      }
    ];

    return (
      <PageHeaderLayout title="售货机管理">
        <Card bordered={ false }>
          <div className={styles.tableList}>
            <Table
              rowKey={ record => record.sri }
              dataSource={ vendingList }
              columns={ columns }
              pagination={ false }
            />
          </div>
        </Card>
      </PageHeaderLayout>
    )

  }

}
