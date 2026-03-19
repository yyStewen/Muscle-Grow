import request from '@/utils/request';

// 用户端当前只开放“查看可购买券”和“购买券”两类动作，后续钱包/核销可继续在这里扩展。
export const getAvailableVouchers = () => {
  return request({
    url: '/user/voucher/list',
    method: 'get'
  });
};

export const purchaseVoucher = (id) => {
  return request({
    url: `/user/voucher/purchase/${id}`,
    method: 'post'
  });
};
