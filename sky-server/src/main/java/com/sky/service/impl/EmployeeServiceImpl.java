package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Api(tags = "员工相关")
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @ApiOperation("员工登录")
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
          password =  password.toUpperCase();
        System.out.println(password);
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO emp) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(emp,employee);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        String pwd = DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()).toUpperCase();
        employee.setPassword(pwd);
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);

    }

    @Override
    public PageResult selectOfPage(EmployeePageQueryDTO emp)
    {
        if(emp.getPage() ==  0)
        {
            emp.setPage(1);
            emp.setPageSize(10);
        }
        PageHelper.startPage(emp.getPage(),emp.getPageSize());
        List<Employee> array =  employeeMapper.selectByName(emp.getName());

        return new PageResult(array.size(),array);
    }

    @Override
    public Employee getById(Long id) {
        var res =  employeeMapper.getById(id);
        res.setPassword("*****");
        return res;

    }

    @Override
    public void update(Employee emp) {
        emp.setUpdateTime(LocalDateTime.now());
        emp.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(emp);
    }

    @Override
    public void update(EmployeeDTO emp)
    {
        Employee employee = new Employee();
        BeanUtils.copyProperties(emp, employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        
        // 如果 DTO 中没有 id，则使用当前登录用户的 id
        if (employee.getId() == null) {
            employee.setId(BaseContext.getCurrentId());
        }
        
        employeeMapper.update(employee);

    }

}
