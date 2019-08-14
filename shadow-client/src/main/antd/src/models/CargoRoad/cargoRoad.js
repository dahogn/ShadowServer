import { notification } from 'antd';
import { getCargoRoadList, getCommodityList, addCargoRoad } from "../../services/CargoRoad/cargoRoad";

export default {
  namespace: 'cargoRoad',

  state: {
    vending: {},
    cargoRoadList: [],
    commodityModalVisible: false,
    commodityList: [],
    cargoRoadId: '',
    addCargoRoadVisible: false,
  },

  effects: {
    *getCargoRoadList({ payload }, { call, put }) {
      const response = yield call(getCargoRoadList, payload);
      yield put({
        type: 'setCargoRoadList',
        payload: response,
      });
    },
    *fetchCommodity({ payload }, { call, put }) {
      const response = yield call(getCommodityList, payload);
      yield put({
        type: 'setCommodityList',
        payload: response,
      });
    },
    *addCargoRoad({ callback, payload }, { call }) {
      const response = yield call(addCargoRoad, payload);
      if (callback) {
        callback();
      }
    },
  },

  reducers: {
    setVending(state, { payload }) {
      return {
        ...state,
        vending: payload,
      };
    },
    setCargoRoadList(state, { payload }) {
      return {
        ...state,
        cargoRoadList: payload,
      };
    },
    setCommodityModalVisible(state, { payload }) {
      return {
        ...state,
        commodityModalVisible: payload,
      };
    },
    setCommodityList(state, { payload }) {
      return {
        ...state,
        commodityList: payload,
      };
    },
    setCargoRoadId(state, { payload }) {
      return {
        ...state,
        cargoRoadId: payload,
      };
    },
    setAddCargoRoadVisible(state, { payload }) {
      return {
        ...state,
        addCargoRoadVisible: payload,
      };
    },
  }
}
