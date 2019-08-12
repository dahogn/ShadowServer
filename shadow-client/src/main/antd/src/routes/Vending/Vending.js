import React, { Component } from "react";
import PageHeaderLayout from '../../layouts/PageHeaderLayout';
import { Table, Card } from "antd";
import styles from '../../common/common.less';
import { connect } from 'dva';

@connect(({ vending }) => ({ vending }))
export default class Vending extends Component {

  componentWillMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'vending/getVendingList',
    });
  }

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
