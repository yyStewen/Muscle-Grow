import request from '@/utils/request';

export const submitOrder = (data) => {
  return request({
    url: '/user/order/submit',
    method: 'post',
    data
  });
};

export const payOrder = (data) => {
  return request({
    url: '/user/order/payment',
    method: 'put',
    data
  });
};

export const getOrderHistory = (params) => {
  return request({
    url: '/user/order/historyOrders',
    method: 'get',
    params
  });
};

export const getOrderDetail = (id) => {
  return request({
    url: `/user/order/orderDetail/${id}`,
    method: 'get'
  });
};

export const cancelOrder = (id) => {
  return request({
    url: `/user/order/cancel/${id}`,
    method: 'put'
  });
};

export const repeatOrder = (id) => {
  return request({
    url: `/user/order/repetition/${id}`,
    method: 'post'
  });
};

export const remindOrder = (id) => {
  return request({
    url: `/user/order/reminder/${id}`,
    method: 'get'
  });
};
