package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(jwtProperties.getAdminTtl(), claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @ApiOperation("新增员工")
    @PostMapping
    public  Result save(@RequestBody EmployeeDTO emp)
    {
        log.info( "新增员工{} ",emp);
        employeeService.save(emp);
        log.info("员工保存成功");
        return Result.success();
    }
    /**
     * 分页查询
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO emp)
    {
        log.info("分页查询，参数：{}", emp);
        return Result.success(employeeService.selectOfPage(emp));
    }
    @PostMapping("status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("员工状态：{}", status);
        log.info("员工id：{}", id);
        Employee emp = Employee.builder().status( status).id( id).build();
        employeeService.update(emp);
        return Result.success();
    }
    @PutMapping
    public Result update(@RequestBody EmployeeDTO emp)
    {
        log.info("员工修改{} ",emp);

        employeeService.update(emp);
        log.info("员工修改成功");
        return Result.success();
    }
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id)
    {
        log.info("员工查询{} ",id);
        Employee emp = employeeService.getById(id);
        return Result.success(emp);
    }
}
