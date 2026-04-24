package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatisticsVO implements Serializable {
    //待接单数量
    private Long toBeConfirmed;

    //待派送数量
    private Long confirmed;

    //派送中数量
    private Long deliveryInProgress;


}
