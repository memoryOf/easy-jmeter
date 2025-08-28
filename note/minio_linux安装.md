## MinIO 在 Linux 上的安装方法

### 1. 下载 MinIO 服务器

```bash
# 下载最新版本的 MinIO 服务器
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
sudo mv minio /usr/local/bin/minio
```


### 2. 创建用户和目录

```bash
# 创建 minio 用户和组
sudo groupadd -r minio-user
sudo useradd -M -r -g minio-user minio-user

# 创建数据存储目录
sudo mkdir -p /data/minio
sudo chown minio-user:minio-user /data/minio
```


### 3. 配置环境变量

创建环境配置文件：
```bash
sudo nano /etc/default/minio
```


添加以下内容：
```bash
# MinIO本地挂载路径
MINIO_VOLUMES="/data/minio"

# MinIO服务监听地址和端口
MINIO_OPTS="--console-address :9090"

# MinIO访问凭证
MINIO_ROOT_USER=admin
MINIO_ROOT_PASSWORD=minio2023
```


### 4. 创建 systemd 服务文件

```bash
sudo nano /etc/systemd/system/minio.service
```


添加以下内容：
```ini
[Unit]
Description=MinIO
Documentation=https://docs.min.io
Wants=network-online.target
After=network-online.target
AssertFileIsExecutable=/usr/local/bin/minio

[Service]
WorkingDirectory=/usr/local/

User=minio-user
Group=minio-user
ProtectProc=invisible

EnvironmentFile=/etc/default/minio
ExecStartPre=/bin/bash -c "if [ -z \"${MINIO_VOLUMES}\" ]; then echo \"Variable MINIO_VOLUMES not set in /etc/default/minio\"; exit 1; fi"
ExecStart=/usr/local/bin/minio server $MINIO_OPTS $MINIO_VOLUMES

# Let systemd restart this service always
Restart=always

# Specifies the maximum file descriptor number that can be opened by this process
LimitNOFILE=65536

# Specifies the maximum number of threads this process can create
LimitNPROC=65536

# Disable timeout logic and wait until process is stopped
TimeoutStopSec=infinity
SendSIGKILL=no

[Install]
WantedBy=multi-user.target
```


### 5. 启动 MinIO 服务

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启动 MinIO 服务
sudo systemctl start minio
# 重启
sudo systemctl restart minio

# 设置开机自启
sudo systemctl enable minio

# 检查服务状态
sudo systemctl status minio
```


### 6. 防火墙配置

```bash
# 如果启用了防火墙，需要开放端口
sudo firewall-cmd --permanent --add-port=9000/tcp
sudo firewall-cmd --permanent --add-port=9090/tcp
sudo firewall-cmd --reload
```


### 7. 访问 MinIO

- API 端口：9000（根据您的配置文件中 `minio.endpoint: http://10.10.77.87:9085`，您可能需要调整端口）
- 控制台端口：9090

### 根据您的配置文件调整

根据您提供的 [application-test.yml](file://D:\tools\JetBrains\IdeaProjects\fengzhao\easy-jmeter\api\src\main\resources\application-test.yml) 配置，您需要：

1. 确保 MinIO 运行在 9085 端口（与配置中的 `minio.endpoint` 匹配）
2. 创建 `dev` 存储桶
3. 使用以下凭证：
    - accessKey: root
    - secretKey: minio2025

修改服务配置中的端口：
```bash
# 在 /etc/default/minio 中添加
MINIO_OPTS="--address :9085 --console-address :9090"
```


安装完成后，您可以访问 `http://your-server-ip:9090` 来使用 MinIO 的 Web 管理界面。