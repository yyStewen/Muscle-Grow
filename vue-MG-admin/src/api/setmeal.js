import request from '@/utils/request';

export const getSetmealPage = (params) => {
  return request({
    url: '/setmeal/page',
    method: 'get',
    params
  });
};

export const setmealCategoryList = (params) => {
  return request({
    url: '/category/list',
    method: 'get',
    params
  });
};

export const getSupplementsByCategory = (categoryId) => {
  return request({
    url: '/setmeal/list',
    method: 'get',
    params: { categoryId }
  });
};

export const createSetmeal = (data) => {
  return request({
    url: '/setmeal',
    method: 'post',
    data
  });
};

export const updateSetmeal = (data) => {
  return request({
    url: '/setmeal',
    method: 'put',
    data
  });
};

export const deleteSetmeal = (ids) => {
  return request({
    url: '/setmeal',
    method: 'delete',
    params: { ids }
  });
};

export const getSetmealById = (id) => {
  return request({
    url: `/setmeal/${id}`,
    method: 'get'
  });
};

export const setmealStatusByStatus = (params) => {
  return request({
    url: `/setmeal/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
  });
};
