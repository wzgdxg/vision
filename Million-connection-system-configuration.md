```plantuml
    # /etc/sysctl.conf
    net.ipv4.tcp_mem = 1572864 2097152 2621440 min pressure max  单位是页，1页4KB
    net.ipv4.tcp_rmem = 4096 87380 16777216    min default max  单位是字节
    net.ipv4.tcp_wmem = 4096 65536 16777216
    fs.file-max = 3000000  # 最大打开文件数，这个是总的
    fs.nr_open = 1536000  # 最大打开文件数，这个是每个进程的上限
    # 全连接队列上限（Netty的backlog要匹配这个值）
    net.core.somaxconn = 65535
    # 允许TIME_WAIT端口复用（核心！必开）
    net.ipv4.tcp_tw_reuse = 1
    # 禁止tcp_tw_recycle（NAT环境下会导致连接异常，必关）
    net.ipv4.tcp_tw_recycle = 0
    # 系统最大TIME_WAIT数量，超出直接清理
    net.ipv4.tcp_max_tw_buckets = 500000
    # 半连接队列（SYN队列）上限，应对大量握手请求
    net.ipv4.tcp_max_syn_backlog = 65535
    # 连接空闲7200秒（2小时）后开始发保活包
    net.ipv4.tcp_keepalive_time = 7200
    # 保活包间隔30秒
    net.ipv4.tcp_keepalive_intvl = 30
    # 重试3次无响应则判定连接死亡
    net.ipv4.tcp_keepalive_probes = 3
    # 开启SYN Cookies，防SYN洪水攻击
    net.ipv4.tcp_syncookies = 1
    # 队列溢出时直接丢弃连接，不卡死内核
    net.ipv4.tcp_abort_on_overflow = 1
    # 缩短FIN断开超时时间（默认60s，改15s）
    net.ipv4.tcp_fin_timeout = 15
    
    # /etc/security/limits.conf   hard 是上限，soft ≤ hard，普通进程默认用 soft 作为自己的上限，是每个进程的
    *       hard    nofile     1536000   用户  限制类型  限制项  值
    *       soft    nofile     1536000  对所有用户生效（启动服务的用户都包含） 软限制/硬限制  最大打开文件数 Linux 中 1 个 TCP 连接 = 1 个文件描述符→ 这个值 = 最大能建立的连接数
    
    额外：# 增大网卡接收/发送队列
    ifconfig eth0 txqueuelen 65535
```
>fs.nr_open = 内核级顶层硬天花板（绝对不能破）  
>limits.conf 的 nofile = 用户层配置上限（受内核天花板管）
