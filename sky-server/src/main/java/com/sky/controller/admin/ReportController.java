package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.admin.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@RestController
@RequestMapping("/admin/report")
public class ReportController
{
    @Autowired
    private ReportService reportService;
    @GetMapping("/turnoverStatistics")
    public Result turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {

    return Result.success(reportService.turnoverStatistics(begin,end));
    }
    /**
     * 用户统计
     */
    @GetMapping("/userStatistics")
    public Result userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {
        return Result.success(reportService.userStatistics(begin,end));
    }
    /**
     * 订单统计
     */
    @GetMapping("/ordersStatistics")
    public Result ordersStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                   @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {
        return Result.success(reportService.ordersStatistics(begin,end));
    }
    /**
     * 销量统计
     */
    @GetMapping("/top10")
    public Result top10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {
        return Result.success(reportService.top10(begin,end));
    }
    /**
     * 获取öt业务数据统计
     */
    @GetMapping("/export")
    public Result getBusinessData(HttpServletResponse response)
    {

        reportService.exportBusinessData(response);
        return Result.success();
    }
}
