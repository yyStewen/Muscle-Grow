import request from '@/utils/request';

const EXCEL_MIME_TYPE =
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
const DEFAULT_EXPORT_FILE_NAME = '\u8fd0\u8425\u6570\u636e\u62a5\u8868.xlsx';

const parseFileName = (contentDisposition) => {
  if (!contentDisposition) {
    return DEFAULT_EXPORT_FILE_NAME;
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }

  const plainMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
  if (plainMatch?.[1]) {
    return decodeURIComponent(plainMatch[1]);
  }

  return DEFAULT_EXPORT_FILE_NAME;
};

const saveBlobFile = (blob, fileName) => {
  const objectUrl = window.URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = objectUrl;
  anchor.download = fileName;
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  window.URL.revokeObjectURL(objectUrl);
};

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

export const exportBusinessReport = () => {
  return request({
    url: '/report/export',
    method: 'get',
    responseType: 'blob'
  });
};

export const downloadBusinessReport = async () => {
  const response = await exportBusinessReport();
  const fileName = parseFileName(response.headers?.['content-disposition']);
  const blob = new Blob([response.data], {
    type: response.data?.type || EXCEL_MIME_TYPE
  });

  saveBlobFile(blob, fileName);
};
