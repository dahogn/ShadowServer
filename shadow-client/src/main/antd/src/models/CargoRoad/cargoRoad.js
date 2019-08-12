import { getCargoRoadList } from "../../services/CargoRoad/cargoRoad";

export default {
  namespace: 'cargoRoad',

  state: {
    vending: {},
    cargoRoadList: [],
  },

  effects: {
    *getCargoRoadList({ payload }, { call, put }) {
      const response = yield call(getCargoRoadList, payload);
      yield put({
        type: 'setCargoRoadList',
        payload: response,
      });
    }
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
    }
  }
}
