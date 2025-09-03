我来帮你了解如何在 Ubuntu 上设置固定 IP 地址，确保重启后 IP 地址保持不变。

在 Ubuntu 系统中，有几种方式可以配置静态 IP 地址。现代 Ubuntu 版本（18.04 及以后）使用 netplan 作为网络配置工具。

## 使用 Netplan 配置静态 IP（推荐方法）

1. 首先查看 netplan 配置文件：



**查看目录失败** */etc*


看起来我无法直接访问你的系统目录。不过我可以给你提供在 Ubuntu 系统中设置静态 IP 的通用方法。

## 在 Ubuntu 中设置静态 IP 地址的方法

### 方法一：使用 Netplan（Ubuntu 18.04 及以后版本）

1. 查看当前网络配置文件：
   ```bash
   ls /etc/netplan/
   ```


2. 编辑 netplan 配置文件（通常以 `.yaml` 结尾）：
   ```bash
   sudo nano /etc/netplan/00-installer-config.yaml
   ```


3. 修改配置文件内容，例如：
   ```yaml
   network:
     version: 2
     ethernets:
       ens33:        # 替换为你的网络接口名称
         dhcp4: false
         addresses:
           - 192.168.1.100/24   # 设置你想要的静态 IP 和子网掩码
         gateway4: 192.168.1.1  # 设置网关
         nameservers:
           addresses:
             - 8.8.8.8
             - 8.8.4.4
   ```


4. 应用配置：
   ```bash
   sudo netplan apply
   ```


### 方法二：使用传统网络配置（Ubuntu 16.04 及以前版本）

1. 编辑网络接口配置文件：
   ```bash
   sudo nano /etc/network/interfaces
   ```


2. 添加或修改配置：
   ```
   auto ens33
   iface ens33 inet static
       address 192.168.1.100
       netmask 255.255.255.0
       gateway 192.168.1.1
       dns-nameservers 8.8.8.8 8.8.4.4
   ```


3. 重启网络服务：
   ```bash
   sudo systemctl restart networking
   ```


### 如何查找网络接口名称

使用以下命令查看网络接口：
```bash
ip addr show
```


或者：
```bash
ifconfig -a
```


### 验证配置

配置完成后，可以使用以下命令验证：
```bash
ip addr show
ping -c 4 8.8.8.8
```


## 注意事项

1. 在设置静态 IP 之前，请确保了解你的网络环境，包括：
    - 网络网段
    - 网关地址
    - DNS 服务器地址
    - 可用的 IP 地址范围

2. 设置静态 IP 时，确保该 IP 不在 DHCP 分配范围内，避免 IP 冲突。

3. 如果是远程连接服务器操作，请确保配置正确，避免连接中断。

这些步骤应该能帮助你在 Ubuntu 系统上设置静态 IP 地址，重启后 IP 地址将保持不变。你使用的是哪个版本的 Ubuntu？这样我可以提供更具体的指导。

要查看你的网络接口名称，可以使用以下几种方法：

## 1. 使用 `ip` 命令（推荐）

```bash
ip addr show
```


或者简写为：
```bash
ip a
```


这个命令会显示所有网络接口及其详细信息，包括接口名称、IP 地址、MAC 地址等。

## 2. 使用 `ifconfig` 命令

```bash
ifconfig -a
```


注意：在一些较新的 Ubuntu 版本中，可能需要先安装 `net-tools` 包：
```bash
sudo apt update
sudo apt install net-tools
```


## 3. 使用 `nmcli` 命令（NetworkManager）

```bash
nmcli device status
```


或者：
```bash
nmcli connection show
```


## 4. 直接查看 `/sys/class/net/` 目录

```bash
ls /sys/class/net/
```


## 输出示例解释

当你运行 `ip addr show` 时，会看到类似这样的输出：

```
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 00:0c:29:8a:bd:17 brd ff:ff:ff:ff:ff:ff
    inet 192.168.1.20/24 brd 192.168.1.255 scope global dynamic ens33
       valid_lft 86000sec preferred_lft 86000sec
3: docker0: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc noqueue state DOWN group default
    link/ether 02:42:1d:ad:7a:56 brd ff:ff:ff:ff:ff:ff
    inet 172.17.0.1/16 brd 172.17.255.255 scope global docker0
       valid_lft forever preferred_lft forever
```


在上面的例子中，网络接口名称分别是：
- `lo`：回环接口
- `ens33`：以太网接口（这是你需要配置静态 IP 的接口）
- `docker0`：Docker 虚拟接口

通常，物理网络接口的名称可能是：
- `ensXX`（例如 ens33、ens37）
- `eth0`、`eth1` 等（较老的系统）
- `enp0s3`、`enp0s8` 等格式

运行上述命令之一，你就可以找到你的网络接口名称了。