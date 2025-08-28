根据新的错误信息`Public Key Retrieval is not allowed`，这是MySQL连接的问题，而不是之前InfluxDB的问题。这个错误通常发生在MySQL 8.0及以上版本中，与SSL连接和公钥检索设置有关。

让我来解决这个问题：




**已查看文件** *application-dev.yml 10-20*


根据新的错误信息，问题是出现在MySQL连接上，错误是"Public Key Retrieval is not allowed"。这是MySQL 8.0版本的一个常见问题，需要在连接URL中添加额外的参数。

解决方案是在MySQL连接URL中添加`allowPublicKeyRetrieval=true`参数。

```yaml
#开发环境配置

server:
  # 服务端口
  port: 5000


spring:
  # 数据源配置，请修改为你项目的实际配置
  datasource:
    username: "root"
    password: "19930123"
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/easy-jmeter?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF8&allowPublicKeyRetrieval=true
  data:
    mongodb:
      database: easyJmeter
      host: 192.168.56.66
      port: 9090
      username: root
      password: mongo2020
      authentication-database: admin
  influx:
    url: http://localhost:8086
    user: admin
    password: Admin@123456
    token: 78cI854nWvFxIyn2crSaZo0kow3R--4eMW-nYvTC7wNeiq-NrFBdq4WvjBwcNCiN1X__37bnOhEdwRvPfKGhMw==
    database: mybucket

# 开启权限拦截
auth:
  enabled: true

# 开启登录要求验证码
login-captcha:
  enabled: false
  secret: "m49CPM5ak@MDXTzbbT_ZEyMM3KBsBn!h"

# 开启http请求日志记录
request-log:
  enabled: true


logging:
  level:
    # web信息日志记录
    web: debug
    # SQL日志记录
    com.zhao.easyJmeter.mapper: debug
    org.springframework.data.mongodb.core.MongoTemplate: DEBUG

minio:
  endpoint: http://localhost:9000
  bucketName: easy-jmeter
  accessKey: admin
  secretKey: Admin@123456

swagger:
  enable: true

socket:
  server:
    host: 0.0.0.0
    port: 9002
    enable: true
  client:
    serverUrl: http://127.0.0.1:9002
    enable: false

cron:
  heartBeat: "0 0/1 * * * ?"
  deleteTaskRecordStorage: "0 0 1 ? * MON"
  recordStatistics: "0 0/30 * * * ? "
  jmeterLogClear: "0 0 2 ? * *"
```


主要修改是在MySQL连接URL中添加了`allowPublicKeyRetrieval=true`参数，这将允许客户端从服务器获取公钥以用于加密连接。

这个错误通常发生在MySQL 8.0及以上版本中，当使用某些认证插件（如caching_sha2_password）时，如果连接需要使用RSA公钥进行加密，但没有允许公钥检索时就会出现这个错误。

除了添加`allowPublicKeyRetrieval=true`外，还可以考虑添加`useSSL=false`（如果已经存在）以禁用SSL，或者如果需要使用SSL，可以设置`useSSL=true`并正确配置SSL证书。

如果你希望使用更安全的连接方式，可以考虑以下替代方案：
1. 在MySQL服务器上使用`mysql_native_password`认证插件
2. 正确配置SSL证书
3. 在MySQL服务器上预先设置好RSA密钥对

但就目前情况而言，添加`allowPublicKeyRetrieval=true`应该是最直接有效的解决方案。