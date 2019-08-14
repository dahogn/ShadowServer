import request from '../../utils/request';

export async function getCargoRoadList(params) {
  return request('/demo/cargoRoad?vendingId=' + params, { method: 'GET' });
}

export async function getCommodityList(params) {
  return request('/demo/commodity?cargoRoadId=' + params, { method: 'GET' });
}

export async function addCargoRoad(params) {
  return request('/demo/cargoRoad', {
    method: 'POST',
    body: params,
  });
}
