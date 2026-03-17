import request from '@/utils/request';

export const getCategoryList = (type) => {
  return request({
    url: '/user/category/list',
    method: 'get',
    params: { type }
  });
};

export const getSupplementsByCategory = (categoryId) => {
  return request({
    url: '/user/dish/list',
    method: 'get',
    params: { categoryId }
  });
};

export const getSetmealsByCategory = (categoryId) => {
  return request({
    url: '/user/setmeal/list',
    method: 'get',
    params: { categoryId }
  });
};

export const getSetmealSupplements = (id) => {
  return request({
    url: `/user/setmeal/supplement/${id}`,
    method: 'get'
  });
};

