import request from '@/utils/request';

export const addCartItem = (data) => {
  return request({
    url: '/user/shoppingCart/add',
    method: 'post',
    data
  });
};

export const getCartList = () => {
  return request({
    url: '/user/shoppingCart/list',
    method: 'get'
  });
};

export const cleanCart = () => {
  return request({
    url: '/user/shoppingCart/clean',
    method: 'delete'
  });
};

export const subCartItem = (data) => {
  return request({
    url: '/user/shoppingCart/sub',
    method: 'post',
    data
  });
};
