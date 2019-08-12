import request from '../../utils/request';

export async function getCargoRoadList(params) {
  return request('/demo/cargoRoad?vendingId=' + params, { method: 'GET' });
}
