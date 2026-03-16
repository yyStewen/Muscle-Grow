import request from "@/utils/request";

/**
 *
 * 员工管理
 *
 **/

// 登录
export const login = (data) => request.post('/employee/login', data);

// 退出
export const userLogout = () => request.post('/employee/logout');

// 员工分页查询
export const getEmployeeList = (params) => request.get('/employee/page', { params });

// 修改---启用禁用接口
export const enableOrDisableEmployee = (params) => request.post(`/employee/status/${params.status}?id=${params.id}`);

// 新增---添加员工
export const addEmployee = (data) => request.post('/employee', data);

// 修改---修改员工
export const editEmployee = (data) => request.put('/employee', data);

// 修改页面反查详情接口
export const queryEmployeeById = (id) => request.get(`/employee/${id}`);