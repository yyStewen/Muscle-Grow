import request from '@/utils/request';

// 管理端优惠券接口保持和后端 /admin/voucher 路由一一对应，便于后续继续拆分能力。
export const getVoucherPage = (params) => {
  return request({
    url: '/voucher/page',
    method: 'get',
    params
  });
};

export const addVoucher = (data) => {
  return request({
    url: '/voucher',
    method: 'post',
    data
  });
};

export const getVoucherById = (id) => {
  return request({
    url: `/voucher/${id}`,
    method: 'get'
  });
};

export const editVoucher = (data) => {
  return request({
    url: '/voucher',
    method: 'put',
    data
  });
};

export const voucherStatusByStatus = (params) => {
  return request({
    url: `/voucher/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
  });
};
