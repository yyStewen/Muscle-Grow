import request from '@/utils/request';

export const getBusinessData = () => {
  return request({
    url: '/workspace/businessData',
    method: 'get'
  });
};

export const getOrderOverview = () => {
  return request({
    url: '/workspace/overviewOrders',
    method: 'get'
  });
};

export const getSupplementOverview = () => {
  return request({
    url: '/workspace/overviewDishes',
    method: 'get'
  });
};

export const getSetmealOverview = () => {
  return request({
    url: '/workspace/overviewSetmeals',
    method: 'get'
  });
};
