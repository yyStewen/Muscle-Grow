import request from '@/utils/request';

// 查询分类列表接口
export const getCategoryPage = (params) => {
  return request({
    url: '/category/page',
    method: 'get',
    params
  });
};

// 删除当前列的接口
export const deleCategory = (id) => {
  return request({
    url: '/category',
    method: 'delete',
    params: { id: id }
  });
};

// 修改接口
export const editCategory = (params) => {
  return request({
    url: '/category',
    method: 'put',
    data: { ...params }
  });
};

// 新增接口
export const addCategory = (params) => {
  return request({
    url: '/category',
    method: 'post',
    data: { ...params }
  });
};

// 修改---启用禁用接口
export const enableOrDisableEmployee = (params) => {
  return request({
    url: `/category/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
  });
};
