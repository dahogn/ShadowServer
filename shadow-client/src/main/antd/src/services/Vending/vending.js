import request from '../../utils/request';

export async function getVendingList() {
  return request('/demo/vending', { method: 'GET' })
}
