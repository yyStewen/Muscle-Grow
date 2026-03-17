import axios from 'axios';
import { ElMessage } from 'element-plus';

const request = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000
});

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message =
      error?.response?.data?.msg || error?.message || '接口请求失败，请稍后重试';
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

export default request;

