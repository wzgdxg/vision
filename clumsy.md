### 只拦截：和 8080 端口相关的「所有双向 TCP 流量」
> tcp and (tcp.DstPort == 8080 or tcp.SrcPort == 8080)