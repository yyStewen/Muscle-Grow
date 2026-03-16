import axios from 'axios'
import { ElMessage } from "element-plus";
import router from "@/router";

//创建axios实例对象
const request = axios.create({
  baseURL: '/api',
  timeout: 600000
})

//axios的请求 request 拦截器, 每次请求获取localStorage中的loginUser, 从中获取到token, 在请求头token中携带到服务端
// axios的请求 request 拦截器
request.interceptors.request.use(
  (config) => {
    let loginUser = JSON.parse(localStorage.getItem('loginUser'));

    // 如果用户已登录
    if (loginUser && loginUser.token) {
      // 若当前请求是登录接口，则阻止请求并跳转到首页
      // 注意：config.url 这里是后端接口的路径，通常是 /employee/login，而不是前端页面的 /login
      if (config.url === '/employee/login') {
        router.push('/'); // 跳转到首页
        return Promise.reject(new Error('用户已登录，无法重复登录'));
      }

      // 正常设置请求头 token
      config.headers.token = loginUser.token;
    }


    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);


//axios的响应 response 拦截器
request.interceptors.response.use(
  (response) => { //成功回调
    return response.data
  },
  (error) => { //失败回调
    //如果响应的状态码为401, 则路由到登录页面
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('loginUser');
      ElMessage.error('登录失效, 请重新登录');
      router.push('/login');
    } else {
      // 兼容在请求拦截器中手动抛出的错误
      ElMessage.error(error.message || '接口访问异常');
    }
    return Promise.reject(error);
  }
)

export default request