package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.annotation.AutoFile;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where employee.username = #{username}")
    Employee getByUsername(String username);

    /**
     *
     * @param employee
     */
    @Override
    @Insert("insert into sky_take_out.employee(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values(#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFile(OperationType.INSERT)
    int insert(Employee employee);

    /**
     * 根据id查询
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
    /**
     * 根据用户名查询员工
     * @param name
     * @return
     */
    Page<Employee> selectByName(Page<Employee> page, String name);

    @AutoFile(OperationType.UPDATE)
    void update(Employee emp);

}
