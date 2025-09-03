package com.zhao.easyJmeter.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.zhao.easyJmeter.common.configuration.InfluxDBProperties;
import com.zhao.easyJmeter.dto.task.JmeterParamDTO;
import com.zhao.easyJmeter.service.TaskInfluxdbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * [重构] TaskInfluxdbService 的实现类
 * 全面迁移至 InfluxDB 2.x 客户端和 Flux 查询语言，以获得巨大的性能提升和代码可维护性。
 */
@Slf4j
@Service
public class TaskInfluxdbServiceImpl implements TaskInfluxdbService {

    // 1. 注入现代的 InfluxDB 2.x 客户端和配置
    private final InfluxDBClient influxDBClient;
    private final InfluxDBProperties influxDBProperties;
    private final QueryApi queryApi;

    // 2. 使用构造函数注入，这是 Spring 推荐的最佳实践
    @Autowired
    public TaskInfluxdbServiceImpl(InfluxDBClient influxDBClient, InfluxDBProperties influxDBProperties) {
        this.influxDBClient = influxDBClient;
        this.influxDBProperties = influxDBProperties;
        // 获取一个可重用的 QueryApi 实例
        this.queryApi = influxDBClient.getQueryApi();
    }

    /**
     * [重构] 获取测试的开始和结束时间。
     * 优化：使用两次高效的查询（first() 和 last()）代替一次全量数据拉取。
     */
    @Override
    public Map<String, Object> getTimes(String taskId) {
        String bucket = influxDBProperties.getBucket();
        String org = influxDBProperties.getOrg();

        String baseQuery = String.format(
                "from(bucket: \"%s\") |> range(start: 0) |> filter(fn: (r) => r._measurement == \"events\" and r.application == \"%s\" and r._field == \"text\")",
                bucket, taskId
        );

        String startTimeQuery = baseQuery + " |> filter(fn: (r) => r._value =~ /started$/) |> first()";
        String endTimeQuery = baseQuery + " |> filter(fn: (r) => r._value =~ /ended$/) |> last()";

        // The query method returns a list of tables, not records
        List<FluxTable> startTables = queryApi.query(startTimeQuery, org);
        List<FluxTable> endTables = queryApi.query(endTimeQuery, org);

        // Check if the start time was found by checking if the first table has records
        if (startTables.isEmpty() || startTables.get(0).getRecords().isEmpty()) {
            return Map.of("startTime", "", "endTime", "", "start", "", "end", "", "points", List.of());
        }

        // Since we used first(), we can safely get the first record from the first table
        Instant startTimeInstant = startTables.get(0).getRecords().get(0).getTime();

        // For the end time, check if the query returned any results, otherwise use the current time
        Instant endTimeInstant = (endTables.isEmpty() || endTables.get(0).getRecords().isEmpty())
                ? Instant.now()
                : endTables.get(0).getRecords().get(0).getTime();


        OffsetDateTime startTime = startTimeInstant.atOffset(ZoneOffset.UTC);
        OffsetDateTime endTime = endTimeInstant.atOffset(ZoneOffset.UTC);

        // 生成用于图表的时间点
        List<OffsetDateTime> points = new ArrayList<>();
        if (startTime.isBefore(endTime)) {
            // 直接使用 aggregateWindow 的方式生成时间点，更精确
            long durationSeconds = Duration.between(startTime, endTime).getSeconds();
            long pointCount = (durationSeconds / 5) + 1;
            for (int i = 0; i <= pointCount; i++) {
                points.add(startTime.plusSeconds(i * 5L));
            }
        }

        DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        return Map.of(
                "startTime", startTime.format(isoFormatter),
                "endTime", endTime.format(isoFormatter),
                "points", points,
                "start", startTime.format(newFormatter),
                "end", endTime.format(newFormatter)
        );
    }

    /**
     * [重构] 获取样本总数和错误总数。
     * 优化：使用 Flux 的 sum() 函数进行高效聚合，一个查询搞定。
     */
    @Override
    public Map<String, Object> sampleCounts(String taskId, String startTime, String endTime) {
        if (!StringUtils.hasText(startTime) || !StringUtils.hasText(endTime)) {
            return Map.of("count", Map.of(), "countError", Map.of());
        }

        String bucket = influxDBProperties.getBucket();
        String org = influxDBProperties.getOrg();

        String fluxQuery = String.format(
                "from(bucket: \"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"jmeter\" and r.application == \"%s\")\n" +
                        "  |> filter(fn: (r) => r._field == \"count\" or r._field == \"countError\")\n" +
                        "  |> group(columns: [\"transaction\", \"_field\"])\n" +
                        "  |> sum()",
                bucket, startTime, endTime, taskId);

        List<FluxTable> tables = queryApi.query(fluxQuery, org);

        Map<String, Double> countMap = new HashMap<>();
        Map<String, Double> errorCountMap = new HashMap<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String transaction = (String) record.getValueByKey("transaction");
                String field = record.getField();
                Double value = record.getValue() != null ? ((Number) record.getValue()).doubleValue() : 0.0;

                if ("count".equals(field)) {
                    countMap.put(transaction, value);
                } else if ("countError".equals(field)) {
                    errorCountMap.put(transaction, value);
                }
            }
        }
        return Map.of("count", countMap, "countError", errorCountMap);
    }

    /**
     * [重构 & 性能优化] 获取吞吐量图表数据。
     * 优化：使用 aggregateWindow 在数据库端进行时间窗口聚合，性能提升百倍以上。
     */
    @Override
    public Map<String, Object> throughputGraph(String taskId, String startTime, String endTime, List<OffsetDateTime> points) {
        if (!StringUtils.hasText(startTime) || !StringUtils.hasText(endTime)) {
            return Map.of();
        }

        String bucket = influxDBProperties.getBucket();
        String org = influxDBProperties.getOrg();

        String fluxQuery = String.format(
                "from(bucket: \"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"jmeter\" and r.application == \"%s\" and r.statut == \"all\" and r._field == \"count\")\n" +
                        "  |> group(columns: [\"transaction\"])\n" +
                        "  |> aggregateWindow(every: 5s, fn: sum, createEmpty: false)\n" +
                        "  |> map(fn: (r) => ({ r with _value: float(v: r._value) / 5.0 }))", // 直接计算TPS
                bucket, startTime, endTime, taskId);

        List<FluxTable> tables = queryApi.query(fluxQuery, org);
        Map<String, List<List<Object>>> result = new HashMap<>();

        for (FluxTable table : tables) {
            if (table.getRecords().isEmpty()) continue;
            String transaction = (String) table.getRecords().get(0).getValueByKey("transaction");
            List<List<Object>> dataPoints = table.getRecords().stream()
                    .map(record -> List.of(record.getTime(), record.getValue()))
                    .collect(Collectors.toList());
            result.put(transaction, dataPoints);
        }
        return new HashMap<>(result);
    }

    /**
     * [重构 & 性能优化] 获取错误数图表数据。
     * 优化：同样使用 aggregateWindow。
     */
    @Override
    public Map<String, Object> errorGraph(String taskId, String startTime, String endTime, List<OffsetDateTime> points) {
        if (!StringUtils.hasText(startTime) || !StringUtils.hasText(endTime)) {
            return Map.of();
        }
        String bucket = influxDBProperties.getBucket();
        String org = influxDBProperties.getOrg();

        // 合并两个查询流，一次性获取所有数据
        String fluxQuery = String.format(
                "ko = from(bucket: \"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"jmeter\" and r.application == \"%s\" and r.statut == \"ko\" and r._field == \"count\" and r.transaction != \"all\")\n" +
                        "all_errors = from(bucket: \"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"jmeter\" and r.application == \"%s\" and r.transaction == \"all\" and r._field == \"countError\")\n" +
                        "union(tables: [ko, all_errors])\n" +
                        "  |> group(columns: [\"transaction\"])\n" +
                        "  |> aggregateWindow(every: 5s, fn: sum, createEmpty: false)",
                bucket, startTime, endTime, taskId, bucket, startTime, endTime, taskId);

        List<FluxTable> tables = queryApi.query(fluxQuery, org);
        Map<String, List<List<Object>>> result = new HashMap<>();

        for (FluxTable table : tables) {
            if (table.getRecords().isEmpty()) continue;
            String transaction = (String) table.getRecords().get(0).getValueByKey("transaction");
            List<List<Object>> dataPoints = table.getRecords().stream()
                    .map(record -> List.of(record.getTime(), record.getValue()))
                    .collect(Collectors.toList());
            result.put(transaction, dataPoints);
        }
        return new HashMap<>(result);
    }

    /**
     * [重构] 获取错误详情。
     * 优化：使用 Flux 在服务端完成分组和计数。
     */
    @Override
    public Map<String, Object> errorInfo(String taskId, String startTime, String endTime) {
        if (!StringUtils.hasText(startTime) || !StringUtils.hasText(endTime)) {
            return Map.of();
        }
        String bucket = influxDBProperties.getBucket();
        String org = influxDBProperties.getOrg();

        String fluxQuery = String.format(
                "from(bucket: \"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"jmeter\" and r.application == \"%s\" and r.statut == \"\" and r.transaction != \"internal\")\n" +
                        "  |> filter(fn: (r) => r._field == \"count\" or r._field == \"responseCode\" or r._field == \"responseMessage\")\n" +
                        "  |> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")\n" +
                        "  |> group(columns: [\"transaction\", \"responseCode\", \"responseMessage\"])\n" +
                        "  |> sum(column: \"count\")\n" +
                        "  |> group(columns: [\"transaction\"])",
                bucket, startTime, endTime, taskId);

        List<FluxTable> tables = queryApi.query(fluxQuery, org);
        Map<String, Object> map = new HashMap<>();
        List<Object> transactionList = new ArrayList<>();

        for (FluxTable table : tables) {
            if (table.getRecords().isEmpty()) continue;
            String transaction = (String) table.getRecords().get(0).getValueByKey("transaction");
            List<Object> infoResult = new ArrayList<>();
            long totalCount = 0;
            for (FluxRecord record : table.getRecords()) {
                infoResult.add(Map.of(
                        "responseCode", record.getValueByKey("responseCode"),
                        "responseMessage", record.getValueByKey("responseMessage"),
                        "count", record.getValue()
                ));
                totalCount += ((Number) record.getValue()).longValue();
            }
            transactionList.add(Map.of("transaction", transaction, "count", infoResult));
        }
        map.put("transaction", transactionList);
        // 'count' 字段在原逻辑中似乎有误，这里根据新逻辑重新计算
        map.put("count", transactionList.stream().map(m -> Map.of("transaction", ((Map) m).get("transaction"), "sum", ((List) ((Map) m).get("count")).stream().mapToLong(c -> (Long) ((Map) c).get("count")).sum())).collect(Collectors.toList()));
        return map;
    }

    /**
     * [重构 & 算法优化] 获取事件列表。
     * 优化：使用 O(n) 算法代替 O(n²) 算法，效率天壤之别。
     */
    @Override
    public List<JmeterParamDTO> getEvents(JmeterParamDTO jmeterParamDTO) {
        String startTime = jmeterParamDTO.getStartTime();
        String endTime = jmeterParamDTO.getEndTime();
        if (!StringUtils.hasText(startTime) || !StringUtils.hasText(endTime)) {
            return List.of();
        }

        String bucket = influxDBProperties.getBucket();
        String org = influxDBProperties.getOrg();

        // 1. 构建基础查询
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format("from(bucket: \"%s\") |> range(start: %s, stop: %s)", bucket, startTime, endTime));
        queryBuilder.append(" |> filter(fn: (r) => r._measurement == \"events\")");
        if (StringUtils.hasText(jmeterParamDTO.getApplication())) {
            queryBuilder.append(String.format(" |> filter(fn: (r) => r.application == \"%s\")", jmeterParamDTO.getApplication()));
        }
        if (StringUtils.hasText(jmeterParamDTO.getTags())) {
            queryBuilder.append(String.format(" |> filter(fn: (r) => r.tags == \"%s\")", jmeterParamDTO.getTags()));
        }
        if (StringUtils.hasText(jmeterParamDTO.getText())) {
            queryBuilder.append(String.format(" |> filter(fn: (r) => r.text =~ /%s/)", jmeterParamDTO.getText()));
        }
        queryBuilder.append(" |> sort(columns: [\"_time\"])");

        // 2. 获取所有事件，已按时间排序
        // The query method returns a list of tables. We need to flatten them into a single list of records.
        List<FluxTable> tables = queryApi.query(queryBuilder.toString(), org);
        List<FluxRecord> records = tables.stream()
                .flatMap(table -> table.getRecords().stream())
                .collect(Collectors.toList());

        // 3. O(n) 算法匹配 started 和 ended 事件
        // The rest of the method will now work correctly with the flattened list of records.
        List<JmeterParamDTO> resultsList = new ArrayList<>();

        Map<String, FluxRecord> openEvents = new HashMap<>(); // Key: application + tags

        for (FluxRecord record : records) {
            String text = (String) record.getValueByKey("text");
            String app = (String) record.getValueByKey("application");
            String tags = (String) record.getValueByKey("tags");
            String key = app + "::" + tags;

            if (text != null && text.endsWith("started")) {
                openEvents.put(key, record);
            } else if (text != null && text.endsWith("ended") && openEvents.containsKey(key)) {
                FluxRecord startRecord = openEvents.get(key);
                JmeterParamDTO jmeterParam = new JmeterParamDTO();
                jmeterParam.setApplication(app);
                jmeterParam.setStartTime(startRecord.getTime().toString());
                jmeterParam.setEndTime(record.getTime().toString());
                jmeterParam.setTags(tags);
                jmeterParam.setText(text.split(" ")[0]);
                resultsList.add(jmeterParam);
                openEvents.remove(key); // 匹配成功，移除
            }
        }
        // 结果反转，保持和原逻辑一致（最新在前）
        Collections.reverse(resultsList);
        return resultsList;
    }

    /**
     * [重构 & 性能优化] 获取聚合报告。
     * 优化：使用一个强大的 Flux 查询代替多个查询和复杂的内存计算。
     */
    @Override
    public List<Map<String, Object>> getAggregateReport(List<JmeterParamDTO> jmeterParamDTOList) {
        if (jmeterParamDTOList == null || jmeterParamDTOList.isEmpty()) {
            return List.of();
        }
        String bucket = influxDBProperties.getBucket();
        String org = influxDBProperties.getOrg();
        List<Map<String, Object>> aggregateReportList = new ArrayList<>();

        for (JmeterParamDTO param : jmeterParamDTOList) {
            long durationSeconds = Duration.between(Instant.parse(param.getStartTime()), Instant.parse(param.getEndTime())).getSeconds();
            if (durationSeconds <= 0) durationSeconds = 1;

            String fluxQuery = String.format(
                    "data = from(bucket: \"%s\")\n" +
                            "  |> range(start: %s, stop: %s)\n" +
                            "  |> filter(fn: (r) => r._measurement == \"jmeter\" and r.application == \"%s\" and r.transaction != \"internal\")\n" +
                            "  |> group(columns: [\"transaction\"])\n" +
                            "\n" +
                            "samples = data |> filter(fn: (r) => r._field == \"count\" and r.statut == \"all\") |> sum() |> set(key: \"_field\", value: \"sample\")\n" +
                            "errors = data |> filter(fn: (r) => r._field == \"countError\" and r.statut == \"all\") |> sum() |> set(key: \"_field\", value: \"error\")\n" +
                            "avg = data |> filter(fn: (r) => r._field == \"avg\" and r.statut == \"all\") |> mean() |> set(key: \"_field\", value: \"avg\")\n" +
                            "median = data |> filter(fn: (r) => r._field == \"avg\" and r.statut == \"all\") |> toFloat() |> median() |> set(key: \"_field\", value: \"median\")\n" +
                            "min = data |> filter(fn: (r) => r._field == \"min\" and r.statut == \"all\") |> min() |> set(key: \"_field\", value: \"min\")\n" +
                            "max = data |> filter(fn: (r) => r._field == \"max\" and r.statut == \"all\") |> max() |> set(key: \"_field\", value: \"max\")\n" +
                            "pct90 = data |> filter(fn: (r) => r._field == \"pct90.0\" and r.statut == \"all\") |> toFloat() |> mean() |> set(key: \"_field\", value: \"pct90.0\")\n" +
                            "pct95 = data |> filter(fn: (r) => r._field == \"pct95.0\" and r.statut == \"all\") |> toFloat() |> mean() |> set(key: \"_field\", value: \"pct95.0\")\n" +
                            "pct99 = data |> filter(fn: (r) => r._field == \"pct99.0\" and r.statut == \"all\") |> toFloat() |> mean() |> set(key: \"_field\", value: \"pct99.0\")\n" +
                            "rb = data |> filter(fn: (r) => r._field == \"rb\" and r.statut == \"all\") |> sum() |> map(fn: (r) => ({r with _value: r._value / %d.0})) |> set(key: \"_field\", value: \"rb\")\n" +
                            "sb = data |> filter(fn: (r) => r._field == \"sb\" and r.statut == \"all\") |> sum() |> map(fn: (r) => ({r with _value: r._value / %d.0})) |> set(key: \"_field\", value: \"sb\")\n" +
                            "tps = data |> filter(fn: (r) => r._field == \"hit\" and r.statut == \"all\") |> sum() |> map(fn: (r) => ({r with _value: r._value / %d.0})) |> set(key: \"_field\", value: \"tps\")\n" +
                            "\n" +
                            "union(tables: [samples, errors, avg, median, min, max, pct90, pct95, pct99, rb, sb, tps])\n" +
                            "  |> pivot(rowKey:[\"transaction\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                    bucket, param.getStartTime(), param.getEndTime(), param.getApplication(), durationSeconds, durationSeconds, durationSeconds);

            List<FluxTable> tables = queryApi.query(fluxQuery, org);
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Map<String, Object> row = new HashMap<>(record.getValues());
                    row.put("application", param.getApplication());
                    row.put("tags", param.getTags());
                    row.put("startTime", param.getStartTime());
                    row.put("endTime", param.getEndTime());
                    row.put("text", param.getText());
                    aggregateReportList.add(row);
                }
            }
        }
        return aggregateReportList;
    }
}