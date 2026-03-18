import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

const request = axios.create({
  baseURL: '/api',
  timeout: 600000
});

request.interceptors.request.use(
  (config) => {
    const loginUser = JSON.parse(localStorage.getItem('loginUser-Admin') || 'null');

    if (loginUser && loginUser.token) {
      if (config.url === '/employee/login') {
        router.push('/');
        return Promise.reject(new Error('用户已登录，无法重复登录'));
      }

      config.headers.token = loginUser.token;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  (response) => {
    if (response.config?.responseType === 'blob') {
      return response;
    }

    return response.data;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('loginUser-Admin');
      ElMessage.error('登录失效，请重新登录');
      router.push('/login');
    } else {
      ElMessage.error(error.message || '接口访问异常');
    }

    return Promise.reject(error);
  }
);

export default request;
