import request from '@/utils/request';

export const registerUser = (data) => {
  return request({
    url: '/user/user/register',
    method: 'post',
    data
  });
};

export const loginUser = (data) => {
  return request({
    url: '/user/user/login',
    method: 'post',
    data
  });
};

export const logoutUser = () => {
  return request({
    url: '/user/user/logout',
    method: 'post'
  });
};
