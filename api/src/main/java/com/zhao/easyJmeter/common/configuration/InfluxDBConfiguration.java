package com.zhao.easyJmeter.common.configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfiguration {

    private static final Logger log = LoggerFactory.getLogger(InfluxDBConfiguration.class);

    @Autowired
    private InfluxDBProperties influxDBProperties;

    @Bean
    public InfluxDBClient influxDBClient() {
        // 使用新的工厂方法创建客户端
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(
                influxDBProperties.getUrl(),
                influxDBProperties.getToken().toCharArray(),
                influxDBProperties.getOrg() // 2.x 需要 Organization
        );

        // 在 2.x 中，我们不叫 "database"，而是 "bucket"
        String bucketName = influxDBProperties.getBucket();
        String orgName = influxDBProperties.getOrg();

        // 最佳实践：检查 Bucket 是否存在，而不是每次都尝试创建
        // 这需要您的 Token 拥有 'read:orgs' 和 'read:buckets' 权限
        try {
            Organization org = influxDBClient.getOrganizationsApi().findOrganizations().stream()
                    .filter(o -> o.getName().equals(orgName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("InfluxDB aOrganization '" + orgName + "' not found."));

            Bucket bucket = influxDBClient.getBucketsApi().findBucketByName(bucketName);
            if (bucket == null) {
                log.warn("Bucket '{}' not found. Attempting to create it.", bucketName);
                // 尝试创建 Bucket，这需要您的 Token 拥有 'write:buckets' 权限
                influxDBClient.getBucketsApi().createBucket(bucketName, org);
                log.info("Successfully created bucket '{}'.", bucketName);
            } else {
                log.info("Bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            // 如果权限不足或发生其他错误，这里会捕获到
            log.error("Error during InfluxDB bucket setup: {}. Please check token permissions and configuration.", e.getMessage());
            // 抛出异常让应用启动失败，这比静默失败更好
            throw new RuntimeException("Failed to configure InfluxDB bucket", e);
        }

        return influxDBClient;
    }
}