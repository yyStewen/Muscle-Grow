package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musclegrow.constant.PasswordConstant;
import com.musclegrow.context.BaseContext;
import com.musclegrow.dto.EmployeeDTO;
import com.musclegrow.dto.EmployeePasswordUpdateDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musclegrow.constant.MessageConstant;
import com.musclegrow.constant.StatusConstant;
import com.musclegrow.dto.EmployeeLoginDTO;
import com.musclegrow.dto.EmployeePageQueryDTO;
import com.musclegrow.entity.Employee;
import com.musclegrow.exception.AccountLockedException;
import com.musclegrow.exception.AccountNotFoundException;
import com.musclegrow.exception.BaseException;
import com.musclegrow.exception.PasswordEditFailedException;
import com.musclegrow.exception.PasswordErrorException;
import com.musclegrow.mapper.EmployeeMapper;
import com.musclegrow.result.PageResult;
import com.musclegrow.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        // 1、根据用户名查询数据库中的数据（使用 MyBatis-Plus LambdaQueryWrapper）
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, username);
        Employee employee = employeeMapper.selectOne(queryWrapper);

        // 2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            // 账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对
        // 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            // 密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            // 账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3、返回实体对象
        return employee;
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 使用 mybatis-plus 实现分页查询
        // 1. 构造分页对象 Page
        Page<Employee> pageInfo = new Page<>(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        // 添加模糊查询条件：判断 name 是否为空，不为空才添加 like 匹配
        queryWrapper.like(StringUtils.hasText(employeePageQueryDTO.getName()),
                Employee::getName, employeePageQueryDTO.getName());

        // 添加排序条件：按照 create_time 降序排序
        queryWrapper.orderByDesc(Employee::getCreateTime);

        // 3. 执行查询
        employeeMapper.selectPage(pageInfo, queryWrapper);

        // 4. 封装结果返回
        long total = pageInfo.getTotal();
        java.util.List<Employee> records = pageInfo.getRecords();

        return new PageResult(total, records);
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        // 对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置账号的状态，默认正常状态 1表示正常 0表示锁定
        employee.setStatus(StatusConstant.ENABLE);

        // 设置密码，默认密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // mybatis-plus 将在 MyMetaObjectHandler 中自动填充
        // createTime、updateTime、createUser、updateUser

        employeeMapper.insert(employee);
    }

    /**
     * 启用禁用员工
     * 
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // update employee set status = ? where id = ?

        /*
         * Employee employee = new Employee();
         * employee.setStatus(status);
         * employee.setId(id);
         */

        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        // 使用 mybatis-plus 的 updateById 方法，动态组装不为 null 的字段进行更新
        employeeMapper.updateById(employee);

    }

    /**
     * 根据id查询员工
     * 
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        return employeeMapper.selectById(id);
    }

    /**
     * 修改员工
     * 
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        // mybatis-plus 将在 MyMetaObjectHandler 中自动填充 updateTime 和 updateUser
        
        // 使用 mybatis-plus 的 updateById 方法，基于传入对象的 id 来更新该对象中有值的字段
        employeeMapper.updateById(employee);

    }

    @Override
    public void updatePassword(EmployeePasswordUpdateDTO employeePasswordUpdateDTO) {
        Long employeeId = BaseContext.getCurrentId();
        if (employeeId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }

        if (!StringUtils.hasText(employeePasswordUpdateDTO.getOldPassword())
                || !StringUtils.hasText(employeePasswordUpdateDTO.getNewPassword())
                || !StringUtils.hasText(employeePasswordUpdateDTO.getConfirmPassword())) {
            throw new PasswordEditFailedException("密码不能为空");
        }

        if (!employeePasswordUpdateDTO.getNewPassword().equals(employeePasswordUpdateDTO.getConfirmPassword())) {
            throw new PasswordEditFailedException("两次输入的新密码不一致");
        }

        if (employeePasswordUpdateDTO.getOldPassword().equals(employeePasswordUpdateDTO.getNewPassword())) {
            throw new PasswordEditFailedException("新密码不能与旧密码相同");
        }

        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        String oldPassword = DigestUtils.md5DigestAsHex(employeePasswordUpdateDTO.getOldPassword().getBytes());
        if (!oldPassword.equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        employee.setPassword(DigestUtils.md5DigestAsHex(employeePasswordUpdateDTO.getNewPassword().getBytes()));
        employeeMapper.updateById(employee);
    }

}
