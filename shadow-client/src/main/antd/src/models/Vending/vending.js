import {
  getVendingList
} from "../../services/Vending/vending";

export default {
  namespace: 'vending',

  state: {
    vendingList: [],
  },

  effects: {
    *getVendingList({}, { call, put }) {
      const response = yield call(getVendingList);
      yield put({
        type: 'setVendingList',
        payload: response,
      });
    }
  },

  reducers: {
    setVendingList(state, { payload }) {
      return {
        ...state,
        vendingList: payload,
      };
    }
  },
}
