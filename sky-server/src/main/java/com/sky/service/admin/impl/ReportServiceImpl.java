package com.sky.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.admin.OrderMapper;
import com.sky.mapper.admin.UserMapper;
import com.sky.service.admin.ReportService;
import com.sky.service.admin.WorkspaceService;
import com.sky.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl extends ServiceImpl<OrderMapper, Orders> implements ReportService
{
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<BigDecimal> turnoverList = new ArrayList<>();
        for (LocalDate i = begin; i .equals(end.plusDays(1))  == false; i = i.plusDays(1))
        {
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            LocalDateTime left = LocalDateTime.of(i,LocalTime.MIN);
            LocalDateTime right = LocalDateTime.of(i,LocalTime.MAX);
            queryWrapper.between(Orders::getOrderTime,left,right)
                        .eq(Orders::getStatus,Orders.COMPLETED);
            BigDecimal turnover = baseMapper.sumAmont(queryWrapper);
            if(turnover == null) turnover = BigDecimal.ZERO;
            dateList.add(i);
            turnoverList.add(turnover);
        }


        String json =  StringUtils.join(dateList,',');
        String data = StringUtils.join(turnoverList,',');
        return TurnoverReportVO.builder()
                .dateList(json)
                .turnoverList(data)
                .build();

    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Long> totalUserList = new ArrayList<>();
        for (LocalDate i = begin; i .equals(end.plusDays(1))  == false; i = i.plusDays(1))
        {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            LocalDateTime left = LocalDateTime.of(i,LocalTime.MIN);
            LocalDateTime right = LocalDateTime.of(i,LocalTime.MAX);
            queryWrapper.between(User::getCreateTime,left,right);
            Long newUser = userMapper.count(queryWrapper);
            if(newUser == null) newUser = 0L;
            dateList.add(i);
            newUserList.add(newUser.intValue());
            Long total = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .le(User::getCreateTime,right)
            );
            totalUserList.add(total);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,','))
                .newUserList(StringUtils.join(newUserList,','))
                .totalUserList(StringUtils.join(totalUserList,','))
                .build();
    }

    //* 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7 拒单
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
         List<Integer> orderCountList = new ArrayList<>();
         List<Integer> validOrderCountList = new ArrayList<>();
         List<Double> orderCompletionRateList = new ArrayList<>();
         BigDecimal total = BigDecimal.ZERO;
        BigDecimal validOrder = BigDecimal.ZERO;
        for (LocalDate i = begin; i .equals(end.plusDays(1))  == false; i = i.plusDays(1))
        {

            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            LocalDateTime left = LocalDateTime.of(i,LocalTime.MIN);
            LocalDateTime right = LocalDateTime.of(i,LocalTime.MAX);
            queryWrapper.between(Orders::getOrderTime,left,right);

            Long orderCount = baseMapper.selectCount(queryWrapper);
            if(orderCount == null) orderCount = 0L;
            orderCountList.add(orderCount.intValue());

            queryWrapper.eq(Orders::getStatus,Orders.COMPLETED);
            Long validOrderCount = baseMapper.selectCount(queryWrapper);
            if(validOrderCount == null) validOrderCount = 0L;
            validOrderCountList.add(validOrderCount.intValue());
            dateList.add(i);
             total = total.add(new BigDecimal(orderCount));
             validOrder = validOrder.add(new BigDecimal(validOrderCount));
        }

        double v = 0;
        if(total.compareTo(BigDecimal.ZERO) != 0)
        {
            v = validOrder.divide(total, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        else
        {
            v = 0;
        }



        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,','))
                .orderCountList(StringUtils.join(orderCountList,','))
                .validOrderCountList(StringUtils.join(validOrderCountList,','))
                .validOrderCount(validOrder.intValue())
                 .orderCompletionRate(v)
                .totalOrderCount(total.intValue())
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end)
    {
        LocalDateTime left = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime right = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = baseMapper.selectSalesTop(left, right);
        var nameList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        var numberList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,','))
                .numberList(StringUtils.join(numberList,','))
                .build();
    }

    /**
     * 导出营业数据
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);

        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            Sheet sheet = excel.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue(begin + "-" + end);

            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover().toString());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());

            Row row4 = sheet.getRow(4);
            row4.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row4.getCell(4).setCellValue(businessDataVO.getUnitPrice().toString());

            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                BusinessDataVO dailyData = workspaceService.getBusinessData(date, date);
                Row dailyRow = sheet.getRow(7 + i);

                dailyRow.getCell(1).setCellValue(date.toString());
                dailyRow.getCell(2).setCellValue(dailyData.getTurnover().toString());
                dailyRow.getCell(3).setCellValue(dailyData.getValidOrderCount());
                dailyRow.getCell(4).setCellValue(dailyData.getOrderCompletionRate());
                dailyRow.getCell(5).setCellValue(dailyData.getUnitPrice().toString());
                dailyRow.getCell(6).setCellValue(dailyData.getNewUsers());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=businessData.xlsx");

            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.flush();
            out.close();
            excel.close();
            in.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
