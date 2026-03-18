import request from '@/utils/request';

export const login = (data) => request.post('/employee/login', data);

export const userLogout = () => request.post('/employee/logout');

export const getEmployeeList = (params) => request.get('/employee/page', { params });

export const enableOrDisableEmployee = (params) =>
  request.post(`/employee/status/${params.status}?id=${params.id}`);

export const addEmployee = (data) => request.post('/employee', data);

export const editEmployee = (data) => request.put('/employee', data);

export const queryEmployeeById = (id) => request.get(`/employee/${id}`);

export const updateCurrentEmployeePassword = (data) =>
  request.put('/employee/password', data);
