import request from '@/utils/request';

export const getTurnoverStatistics = (params) => {
  return request({
    url: '/report/turnoverStatistics',
    method: 'get',
    params
  });
};

export const getUserStatistics = (params) => {
  return request({
    url: '/report/userStatistics',
    method: 'get',
    params
  });
};

export const getOrdersStatistics = (params) => {
  return request({
    url: '/report/ordersStatistics',
    method: 'get',
    params
  });
};

export const getSalesTop10 = (params) => {
  return request({
    url: '/report/top10',
    method: 'get',
    params
  });
};
