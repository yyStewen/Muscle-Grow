import request from '@/utils/request';

export const getAddressList = () => {
  return request({
    url: '/user/addressBook/list',
    method: 'get'
  });
};

export const getAddressById = (id) => {
  return request({
    url: `/user/addressBook/${id}`,
    method: 'get'
  });
};

export const createAddress = (data) => {
  return request({
    url: '/user/addressBook',
    method: 'post',
    data
  });
};

export const updateAddress = (data) => {
  return request({
    url: '/user/addressBook',
    method: 'put',
    data
  });
};

export const setDefaultAddress = (id) => {
  return request({
    url: '/user/addressBook/default',
    method: 'put',
    data: { id }
  });
};

export const deleteAddress = (id) => {
  return request({
    url: '/user/addressBook',
    method: 'delete',
    params: { id }
  });
};
