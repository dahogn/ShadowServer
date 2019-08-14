import React, { Component } from 'react';
import { Form, Input, Modal, InputNumber } from 'antd';
import { connect } from 'dva';

const FormItem = Form.Item;

@connect(({ cargoRoad }) => ({ cargoRoad }))
@Form.create()
export default class AddCargoRoadModal extends Component {

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
    const { modalVisible, handleModalVisible, cargoRoad: { vending: { sri } } } = this.props;
    const formItemLayout = {
      labelCol: { span: 4, offset: 1 },
      wrapperCol: { span: 14 },
    };

    return (
      <Modal
        title="添加货道"
        centered
        visible={ modalVisible }
        onOk={ this.okHandler }
        onCancel={ () => handleModalVisible(false) }
        destroyOnClose={ true }
        maskClosable={ false }
        closable={ false }
        okText='保存'
      >
        <div>
          <Form>
            <FormItem {...formItemLayout} label="">
              {getFieldDecorator('vendingSri', { initialValue: sri })(
                <Input disabled={true} type="hidden" />
              )}
            </FormItem>
            <FormItem {...formItemLayout} label="货道序号" required={true}>
              {getFieldDecorator('serial')(
                <InputNumber
                  placeholder="输入序号"
                  min={0}
                  step={1}
                  precision={0}
                />
                )}
            </FormItem>
          </Form>
        </div>
      </Modal>
    )
  }

}
