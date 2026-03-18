package com.musclegrow.service;

import com.musclegrow.dto.EmployeeDTO;
import com.musclegrow.dto.EmployeeLoginDTO;
import com.musclegrow.dto.EmployeePasswordUpdateDTO;
import com.musclegrow.dto.EmployeePageQueryDTO;
import com.musclegrow.entity.Employee;
import com.musclegrow.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    void startOrStop(Integer status, Long id);

    Employee getById(Long id);

    void update(EmployeeDTO employeeDTO);

    void updatePassword(EmployeePasswordUpdateDTO employeePasswordUpdateDTO);
}
