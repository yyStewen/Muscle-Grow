import request from '@/utils/request';

export const getOrderPage = (params) => {
  return request({
    url: '/order/conditionSearch',
    method: 'get',
    params
  });
};

export const getOrderStatistics = () => {
  return request({
    url: '/order/statistics',
    method: 'get'
  });
};

export const getOrderDetail = (id) => {
  return request({
    url: `/order/details/${id}`,
    method: 'get'
  });
};

export const confirmOrder = (data) => {
  return request({
    url: '/order/confirm',
    method: 'put',
    data
  });
};

export const rejectOrder = (data) => {
  return request({
    url: '/order/rejection',
    method: 'put',
    data
  });
};

export const cancelOrder = (data) => {
  return request({
    url: '/order/cancel',
    method: 'put',
    data
  });
};

export const deliverOrder = (id) => {
  return request({
    url: `/order/delivery/${id}`,
    method: 'put'
  });
};

export const completeOrder = (id) => {
  return request({
    url: `/order/complete/${id}`,
    method: 'put'
  });
};
