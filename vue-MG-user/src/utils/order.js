export const ORDER_STATUS_META = {
  1: { label: 'Pending Payment', type: 'warning' },
  2: { label: 'Pending Merchant', type: 'primary' },
  3: { label: 'Accepted', type: 'success' },
  4: { label: 'In Delivery', type: 'success' },
  5: { label: 'Completed', type: 'success' },
  6: { label: 'Cancelled', type: 'info' }
};

export const PAYMENT_STATUS_META = {
  0: { label: 'Unpaid', type: 'info' },
  1: { label: 'Paid', type: 'success' },
  2: { label: 'Refunded', type: 'warning' }
};

export const getOrderStatusMeta = (status) =>
  ORDER_STATUS_META[Number(status)] || { label: 'Unknown', type: 'info' };

export const getPaymentStatusMeta = (status) =>
  PAYMENT_STATUS_META[Number(status)] || { label: 'Unknown', type: 'info' };

export const formatOrderDateTime = (value) => {
  if (!value) return '--';
  return String(value).replace('T', ' ');
};

export const buildOrderSummary = (order) => {
  if (order?.orderSupplements) {
    return order.orderSupplements;
  }

  const details = Array.isArray(order?.orderDetailList) ? order.orderDetailList : [];
  return details
    .map((item) => {
      const spec = item.supplementDetail ? ` (${item.supplementDetail})` : '';
      return `${item.name}${spec} x${item.number}`;
    })
    .join('; ');
};

export const hasRepeatableOrderItems = (order) => {
  const details = Array.isArray(order?.orderDetailList) ? order.orderDetailList : [];
  return details.some((item) => item?.supplementId || item?.setmealId);
};
