import React, { Component } from 'react';
import { Form, Input, Modal, message } from 'antd';
import CommodityTableForm from './CommodityTableForm';
import { connect } from 'dva';

const FormItem = Form.Item;

@connect(({ cargoRoad }) => ({ cargoRoad }))
@Form.create()
export default class CommodityModal extends Component{

  okHandler = e => {
    const { validateFields } = this.props.form;
    const { onSubmit } = this.props;
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        onSubmit(values);
      }
    });
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    const { modalVisible, handleModalVisible, cargoRoad: { cargoRoadId, commodityList } } = this.props;
    const formItemLayout = {
      labelCol: { span: 4, offset: 1 },
      wrapperCol: { span: 14 },
    };

    return (
      <Modal
        title="商品"
        visible={ modalVisible }
        onOk={ this.okHandler }
        onCancel={ () => handleModalVisible(false) }
        destroyOnClose={ true }
        maskClosable={ false }
        closable={ false }
        okText='保存'
        width={800}
      >
        <div>
          <Form>
            <FormItem {...formItemLayout} label="">
              {getFieldDecorator('cargoRoadId', { initialValue: cargoRoadId })(
                <Input disabled={true} type="hidden" />
              )}
            </FormItem>
          </Form>
          <div>
            {getFieldDecorator('commodityList', {
              initialValue: commodityList,
            })(<CommodityTableForm/>)}
          </div>
        </div>
      </Modal>
    )
  }

}
