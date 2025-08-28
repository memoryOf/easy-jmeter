package com.zhao.easyJmeter.service;

import com.zhao.easyJmeter.model.StatisticsDO;

import java.util.Map;

public interface CommonService {

    Map<String, Object> getEnum();

    Map<String, Object> getTotal();

    StatisticsDO getStatisticsById(String id);
}
