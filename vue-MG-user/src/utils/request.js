import axios from 'axios';
import { ElMessage } from 'element-plus';

import router from '@/router';
import { clearAuthState, getToken } from '@/stores/auth';

const request = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000
});

request.interceptors.request.use((config) => {
  const token = getToken();

  if (token) {
    config.headers = config.headers || {};
    config.headers.authentication = token;
  }

  return config;
});

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error?.response?.status;

    if (status === 401) {
      const redirect = router.currentRoute.value?.fullPath || '/user/home';
      clearAuthState();

      if (router.currentRoute.value?.path !== '/login') {
        router.replace({
          path: '/login',
          query: {
            redirect
          }
        });
      }

      ElMessage.error('Session expired. Please log in again.');
      return Promise.reject(error);
    }

    const message =
      error?.response?.data?.msg || error?.message || 'Request failed. Please try again later.';
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

export default request;
