package com.zhao.easyJmeter.common.configuration;

import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class InfluxDBConfiguration {

    @Autowired
    private InfluxDBProperties influxDBProperties;

//    @Bean
//    public InfluxDB influxDB() {
//        InfluxDB influxDB = InfluxDBFactory.connect(influxDBProperties.getUrl(), influxDBProperties.getUser(), influxDBProperties.getPassword());
//        influxDB.query(new Query("CREATE DATABASE " + influxDBProperties.getDatabase()));
//        influxDB.setDatabase(influxDBProperties.getDatabase());
//        return influxDB;
//    }

    @Bean
    public InfluxDB influxDB() {
        InfluxDB influxDB;

        // 如果配置了token，则使用token方式连接（适用于InfluxDB 2.x）
        if (influxDBProperties.getToken() != null && !influxDBProperties.getToken().isEmpty()) {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);

            influxDB = InfluxDBFactory.connect(
                    influxDBProperties.getUrl(),
                    okHttpClientBuilder);
        } else {
            // 否则使用用户名/密码方式连接（适用于InfluxDB 1.x）
            influxDB = InfluxDBFactory.connect(
                    influxDBProperties.getUrl(),
                    influxDBProperties.getUser(),
                    influxDBProperties.getPassword());
        }

        try {
            influxDB.query(new Query("CREATE DATABASE " + influxDBProperties.getDatabase()));
            influxDB.setDatabase(influxDBProperties.getDatabase());
        } catch (Exception e) {
            System.out.println("Warning: Could not create database: " + e.getMessage());
        }

        return influxDB;
    }

}
