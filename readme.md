# 总结
1. 一个连接（Channel） → 终身绑定一个线程  
   连接的所有读写操作，永远只由这一个线程执行  
   无多线程竞争 → 不需要同步、不需要加锁  
   这就是 Netty 高性能的核心秘密！  
2. 不要让回调阻塞了 Netty 的 EventLoop 线程，导致请求和业务逻辑都堵塞。
3. ByteBuf的markReaderIndex和resetReaderIndex方法特别有用，mark后不管buf被当做参数传递到哪，调用reset后都可以恢复到之前的状态。  
   markWriterIndex和resetWriterIndex同理。
4. 入站处理器：异常从链的头部向尾部传播（A → B → C）  
   出站处理器：异常从链的尾部向头部传播（C → B → A） 
5. Adapter的exceptionCaught都会调用 ctx.fireExceptionCaught() 方法，将异常传递给下一个处理器。 
6. 异常捕获：   
   + 同步异常：在处理器方法中直接抛出的异常会被 Netty 捕获并调用 exceptionCaught 
   + 异步异常：异步操作的异常会被捕获并设置到 ChannelFuture 中，需要通过监听器处理 
   + 异常传播：如果 exceptionCaught 调用了 ctx.fireExceptionCaught(cause)，异常会继续传播
7. Handler和Adapter的选择：
   + Adapter 最基础、最原生、什么都不帮你做
   + Handler 高级封装、自动帮你释放内存、自动类型转换
   + 选 SimpleChannelInboundHandler<T>    
      + 你处理的是字符串、对象、ByteBuf 
      + 你不想管内存释放（新手首选，不会泄漏） 
      + 你希望自动类型转换 
      + 你写业务逻辑（90% 场景都用这个）
   + 选 ChannelInboundHandlerAdapter
      + 你要完全控制 ByteBuf（不自动释放） 
      + 你要透传消息（不消费、不释放） 
      + 你在写解码器、重放器、特殊流处理 
      + 你需要手动控制生命周期
8. handler执行顺序，通过last添加，入栈：先添加先执行，出栈：后添加后执行，也可以说是入栈队头到队尾，出栈队尾到队头
9. Netty 发送消息的两种写法，唯一区别：出站事件在 Pipeline 里的【起点不一样】
    
    | 写法 | 出站传播起点 | 效果 |
    | --- | --- | --- |
    | ctx.writeAndFlush(msg) | 当前 Handler 的下一个节点 | 跳过前面的处理器，向后传播 |
    | channel().writeAndFlush(msg) | 整个 Pipeline 的尾部（tail） | 从头回溯所有出站处理器 |

10.有阻塞业务逻辑，千万别用eventLoopGroup，否则会导致请求和业务逻辑都堵塞。可以添加一个独立的业务线程池，专门处理阻塞逻辑。
```java
// 1. 创建独立的业务线程池（专门处理阻塞逻辑）
EventExecutorGroup businessPool = new DefaultEventExecutorGroup(10);

// 2. 添加 Handler 时，绑定这个业务线程池
pipeline.addLast(businessPool, new MyBlockHandler());
```
11. Netty EventExecutorGroup 是一个定制化的线程池，用于处理有阻塞的操作，如文件读写、数据库操作等。

    | 维度 | Netty EventExecutorGroup | Java 原生线程池 (ThreadPoolExecutor) |
    | --- | --- | --- |
    | 设计目的 | 专门执行 ChannelHandler 中的阻塞逻辑 | 执行通用异步任务，无 Netty 适配 |
    | 线程绑定 | ✅ 固定线程绑定一个 Handler/Channel 永远只分配同一个线程 | ❌ 随机分配线程每次任务可能用不同线程 |
    | 线程安全 | 天然安全，无需加锁（单线程执行） | 多线程并发，必须加锁（synchronized / 原子类） |
    | 事件顺序 | 保证任务FIFO 有序执行 | 任务无序，可能乱序 |
    | Netty 集成 | 无缝适配 EventLoop、Pipeline、生命周期 | 无集成，需要手动管理上下文 |
    | 使用场景 | Netty Handler 阻塞业务（首选） | 普通业务异步、非 Netty 场景 |
12. 业务线程可以直接操作channel的write/close/flush，且是多线程安全的。但handler相关处理还是需要在eventLoop线程中执行。
    ```plantuml
    【业务线程1】 ──┐
    【业务线程2】 ──┼──→ 【线程安全队列 MPSC】 → 【唯一IO线程】串行执行
    【业务线程3】 ──┘
    ```
13. native方法不一定慢  
    因为普通 native 方法确实慢：  
    进入 native 需要状态切换  
    不能被 JIT 优化  
    不能用寄存器  
    但 Unsafe 类所有方法都是特殊的：  
    JIT 直接编译成单条机器指令  
    零方法调用开销  
    零 native 切换开销  
    Unsafe 里绝大多数 native 方法：完全不触发内核态切换，全程用户态  
    只有极少数「分配 / 释放堆外内存」的 Unsafe 方法：会进内核、会切换态 
14. ByteBufHolder 是一个接口，业务自定义消息体可以实现它（主要是避免每次都解析整个byteBuf），用于提供除bytebuf以外额外的version，cmd等业务信息（byteBuf的包装类），
    实现了ByteBufHolder接口，在sampleHandler里会自动释放里面的bytebuf，adapter还是需要手动释放。里面的content最好是readRetainedSlice来的，
    避免使用原byteBuf，且Retained计数，防止被释放。
15. 为什么ByteBufAllocator只能通过channel获取（ctx.alloc()也是通过channel获取的）：
    + 为了无锁分配byteBuf(高性能池化)，避免了多线程并发分配的开销。byteBufAllocator是和eventLoop绑定的，每个eventLoop都有自己的byteBufAllocator，多个channel共享同一个eventLoop即共享同一个byteBufAllocator。
16. 百万连接下，注意背压，注意设置高低水位，避免内存泄漏。 channelWritabilityChanged事件，用于监听channel写缓冲水位，避免写入写入过快，导致大连接量下OOM。
 百万连接下内存配置预估：

    | 用途 | 占用                             | 归属                                                                                                    |
    | --- |--------------------------------|-------------------------------------------------------------------------------------------------------|
    | JVM 堆 | 4G                             | 业务 / 连接对象，这里必须分批发送，不然4G也不够                                                                            |
    | Netty 直接内存 | 2G-8G                          | 编解码 / 写缓冲，一般2G够了，瞬间全量广播且部分拥堵情况下4G也够，极端是需要32G的，因为每个channel写缓冲水位上限是32K                                  |
    | Linux 内核 + 系统进程 | 1G                             | 系统进程，内核缓存等                                                                                            |
    | TCP 内核连接结构体 | 3.3G                           | 每个TCP 内核连接结构体占3.3K，100万需要3.3G内存 |
    | TCP 内核 Socket 缓冲区 | 无数据收发时物理内存为0，只有真正产生流量时才会分配到8G。 | 每个TCP连接都有自己的读缓冲和写缓冲，默认最小单向4K双向则8K,100万就是4G~8G，开启TCP 优化、大窗口模式时还会翻倍，如果全局大量活跃发送，还会更大，但整体受 tcp_mem 强制全局限流 |
    | JVM 非堆 + 页缓存 + 兜底 | 2G                             | JVM虚拟机堆外内存Metaspace，线程栈，CodeCache等；操作系统还有页缓存等                                                         |
    | 总计 | 要稳定最小20~26G                    | 物理机                                                                                                   |
    
    参考配置：[百万连接系统配置优化](./Million-connection-system-configuration.md)。  
    Netty 读写缓冲 + TCP 内核 Socket 缓冲，全部都是：懒创建（用的时候才分配） + 动态伸缩（闲的时候缩到最小，TCP 4K，netty 只保留缓冲空队列几十字节）  
    没有数据收发，就几乎不占额外内存！   
    最好批量发送，限流，避免大连接量下直接内存和堆内存OOM，也防止tcp缓冲区太大。  
17. 可以通过添加jvm参数，开启netty内存泄漏检测：java -Dio.netty.leakDetectionLevel=paranoid

    | 级别 | 中文释义 | 说明 |
    | :-- | :-- | :-- |
    | DISABLED | 关闭 | 完全禁用内存泄漏检测，可消除 1% 的性能开销；仅建议在充分测试后使用。 |
    | SIMPLE | 简易 | 仅报告是否存在泄漏，采用 1% 采样率；为默认级别，适用于绝大多数场景。 |
    | ADVANCED | 高级 | 报告是否存在泄漏，并记录消息被访问的代码位置；同样使用 1% 采样率。 |
    | PARANOID | 严苛 | 功能同 ADVANCED，但对每一次访问都采样检测；对性能影响极大，仅用于调试阶段。 |

18. TCP三次握手和四次挥手  
    三次握手（建立连接）  

    | 步骤 | 方向        | 发送标志 | 发起端状态变化                | 接收端状态变化                 |
    | --- |-----------| --- |------------------------|-------------------------|
    | 1 | 发起端 → 接收端 | SYN | CLOSED → SYN\_SENT     | LISTEN → SYN\_RCVD      |
    | 2 | 接收端 → 发起端 | SYN+ACK | \-                     | \-                      |
    | 3 | 发起端 → 接收端 | ACK | SYN\_SENT → ESTABLISHED | SYN\_RCVD → ESTABLISHED |
    
    四次挥手（关闭连接）  

    | 步骤 | 方向        | 发送标志 | 状态变化                                                                 |
    | --- |-----------| --- |----------------------------------------------------------------------|
    | 1 | 发起端 → 接收端 | FIN | 发起端：ESTABLISHED → FIN\_WAIT\_1                                       |
    | 2 | 接收端 → 发起端 | ACK | 接收端：ESTABLISHED → CLOSE\_WAIT  <br/> 发起端：FIN\_WAIT\_1 → FIN\_WAIT\_2 |
    | 3 | 接收端 → 发起端 | FIN | 接收端：CLOSE\_WAIT → LAST\_ACK                                          |
    | 4 | 发起端 → 接收端 | ACK | 发起端：FIN\_WAIT\_2 → TIME\_WAIT  <br/> 接收端：收到ACK直接关闭，发起端还会等待2MSL       |

    RST 强制关闭（异常断开）  
    任意一方直接发送：RST  
    双方跳过所有挥手状态，直接从当前状态 → CLOSED  
    无 TIME_WAIT，无协商，立即释放连接  
    对端应用会收到 “连接被重置” 错误  

**注意：  
不要有客户端和服务端的概念，客户端和服务端是相对的，
实际开发中，比如web服务，建立连接确实是客户端发起的，但是断开连接也有可能是服务端发起的，
比如Tomcat / Nginx 配置了 keep-alive 超时时间，超过时间没有数据交互，服务端会主动断开连接。
服务端主动发 FIN → 服务端出现大量 TIME_WAIT**
> 但是这种不占用服务端临时端口、不影响新连接接入，属于正常现象，无需处理； 只有服务端主动外联（DB / 微服务）产生的 TIME_WAIT 堆积，才需要优化。

19. Unpooled.wrappedBuffer(byte[]) 就是把一个普通的 Java 字节数组 byte[]，包装成 Netty 能发送的 ByteBuf 对象。  
    它不会把 byte [] 复制一份，而是直接引用 byte []。返回的 ByteBuf 是堆内存 Buffer发送完 Netty 会自动释放
20. 如果不想释放，可以使用 Unpooled.unreleasableBuffer(ByteBuf)，返回的 ByteBuf 不会自动释放。可以套在堆内存和直接内存上，只是套一层，底层还是原先那个byteBuf
21. HttpServerCodec把请求（拆包、解码）组装成（客户端先开发 → HttpRequest（只有头）
    客户端再发体 → HttpContent（body 片段）
    最后发结束 → LastHttpContent）三段，可以选择性的使用HttpObjectAggregator是否拼成完整的消息FullHttpRequest。中间加个HttpServerExpectContinueHandler来处理100 Continue。
22. 数据流转：

    | 方式 | 拷贝次数                                   | CPU 参与 | 算不算零拷贝 |
    | --- |----------------------------------------| --- | --- |
    | DefaultFileRegion(sendfile) | 0次  磁盘文件 → 由DMA拷贝到，系统文件页缓存 → 有DMA拷贝到网卡 | 完全不参与 | ✅ 真正零拷贝 |
    | DirectBuffer 手动 write | 1 次（用户态直接内存→内核socket缓冲区）               | 参与一次 | ❌ 不算，是减少拷贝 |
    | HeapBuffer(wrapped) write | 2 次（堆→用户态临时直接内存→内核socket缓冲区）           | 参与两次 | ❌ 最差 |

23. SimpleChannelInboundHandler 根据泛型来确定是否由当前handler来处理消息。
     ```java
    try {
        if (acceptInboundMessage(msg)) {   //适配了，才处理
            @SuppressWarnings("unchecked")
            I imsg = (I) msg;
            channelRead0(ctx, imsg);
        } else {
            release = false;
            ctx.fireChannelRead(msg);  //不适配，直接转发给下一个handler
        }
    } finally {
        if (autoRelease && release) {
            ReferenceCountUtil.release(msg);
        }
    }
    
    ```
24. WebSocketServerProtocolHandler 协议升级成功后，只需要移除自定义HttpHandler，程序中channel里的Http相关处理器不需要移除。  
    HttpServerCodec 会被替换成WebSocket相关。  
    HttpServerExpectContinueHandler 非HttpRequest的msg不处理
    HttpObjectAggregator 非HttpObject的msg不处理。    
    ChunkedWriteHandler 非ChunkedInput的msg不处理。 
25. channelGroup.writeAndFlush(msg.retain()); 群发必须retain一次，否则会释放msg，导致群发失败。 传递进来的msg 在handler方法里结束，就会被释放，导致来不及群发完就释放了。
    > 注意1.但自己new出来的不一样，release动作在群发完上（其中每个channel 自己retain和release互相抵消），而新new的在handler方法结束不会release，所有自己 new 出来的出站消息，无论单发 / 群发，writeAndFlush 执行完成后，Netty 自动调用 release ()。 例如：group.writeAndFlush(new TextWebSocketFrame("Client " + ctx.channel() + " joined"));  
      注意2.DefaultChannelGroup下，当channel关闭时，ChannelGroup会自动处理该Channel的移除（会自动监听Channel的关闭事件）。
26. IdleStateHandler必须放在SSLHandler后面，且处理的WebSocketIdlePingHandler必须放在最后，至少是WebSocketServerProtocolHandler之后，因为，WebSocketServerProtocolHandler实现了in和outbound消息的处理，又因为out顺序是倒叙，所以必须放在最后，才能被WebSocketServerProtocolHandler编码成WebSocketFrame。
27. 心跳设多久合适

    | 场景 | 建议间隔 | 原因 |
    | :-- | :-- | :-- |
    | 移动端（4G/5G） | 15-30 秒 | 运营商 NAT 超时常在 30-60 秒 |
    | Web 端（家用宽带） | 30-45 秒 | 路由器 NAT 通常在 60-90 秒 |
    | 企业内网 | 60 秒 | 防火墙可能更宽松 |
    | 配合 TCP Keepalive | 应用层 30 秒 + TCP 5 分钟 | 双层保险 |
> Nginx 默认 proxy_read_timeout 60s

28. event事件
    1.  **读数据、用户事件（心跳）→ 从上往下走（入站）**
    2.  **写数据、发消息 → 从下往上走（出站）**
    3.  **入站、出站完全隔离，互不串门**
    4.  **不 fire 事件，就截断，不传递**
29. 开启ssl必须配置会话，浏览器默认会复用会话id，如果不开启，第二次访问就会无法打开，服务端报错浏览器发来的Received fatal alert: certificate_unknown 证书不可信。
netty默认是不开启ssl会话的，需要配置。
    ```java
    // 创建 SSL 上下文
    context = SslContextBuilder.forServer(kmf)
    // 兼容现代浏览器 TLS1.2 + TLS1.3
                        .protocols(SslProtocols.TLS_v1_2, SslProtocols.TLS_v1_3)
    // Session ID 内存会话缓存（解决刷新bug + 性能提速）
                        .sessionCacheSize(4096)  //最多多少个 Session ID 缓存由 Netty/JDK 全自动管理，你不需要写任何代码干预； 达到上限不会拒绝连接，而是自动淘汰最久没用的会话（LRU 算法）
                        .sessionTimeout(7200) // 2小时，对齐系统TCP保活默认时长  只要成功复用一次，服务端内存里的时间自动重置回 2 小时。
                        .build(); 
    ```
> 也就是开启sessionCacheSize >0，  服务器只会返回会话已过期expired_session ，不会在返回unrecognized_session（这个会导致浏览器中断请求报错）  
> 25 万 Session ≈ 20~30MB 内存
30. TLS 会话复用，全世界只有这两种方案

    | 对比项 | Session ID（会话 ID） | Session Ticket（会话票据） |
    | --- | --- | --- |
    | 存储方 | 服务端内存 | 浏览器本地 |
    | 是否有状态 | 有状态 | 无状态 |
    | 配置参数 | sessionCacheSize、sessionTimeout | sessionTicketKeys 密钥 |
    | JDK 原生 SSL 支持 | ✅ 全部支持 | ❌ 完全不支持 |
    | OpenSSL 支持 | ✅ | ✅ |
    | 集群部署 | 差（内存不互通） | 完美（密钥通用即可） |
    | 超时续期 | 复用成功自动重置时间 | 内置有效期，无法续期 |
    | Netty 默认 | 默认关闭缓存（坑源） | 默认关闭，且需要额外依赖 |
    | 对你项目的作用 | 根治你证书报错的唯一关键 | 本地开发完全用不上 |

31. nginx和其他负载均衡器SLB/CLB 除了短链接会均衡漂移，长连接不管是http还是ws，还是所有tcp，只在【TCP 三次握手建立连接的那一瞬间】决策一次，之后这条水管里跑什么应用流量，网关一概不再重新分配服务器，除非断开重连。
32. httpHandler放在WebSocketStatusAndEventHandler之前，在WebSocket握手完成后再移除WebSocketStatusAndEventHandler，这是netty官方推荐的做法。
33. 断网或者100%丢包，可以实现代码控制，操作系统也会保底，tcp重传多次，未收到ack，会自动断开Connection reset。linux默认10轮，总耗时10到12分钟，windows不好配置，且短暂多了。
> 操作系统只会重传，而不会重连，重连是应用层的逻辑。
34. WebSocket客户端pipeline也必须要有HttpClientCodec相关的（因为WebSocket握手是http协议），还有WebSocketClientProtocolHandler，以及WebSocketClientHandshaker，但WebSocketClientProtocolHandler可以动态加入，且加入后就会自动发起WebSocket握手。
35. WebSocket的心跳检测，两端都做即可，实现idleStateHandler，但空闲触发时间不要一样，一个长一个短，这样虽然实现了两端，但真正触发只有一端。
> 注意：JS WebSocket API 无法主动发送 Ping 帧，浏览器的硬性规定。
36. 只要调用了 connect()，无论 TCP 连接成功 / 失败，都会创建临时 Channel，失败后立刻触发 channelInactive() 方法。
> 所以channelInactive里加重连，但是要注意，不能无限重连，否则会陷入死循环。

# 开发经验
1. jdk17下，如果报错ClassCircularityError: java/lang/WeakPairMap$Pair$Weak，原因不是程序问题，是jdk问题https://github.com/alibaba/transmittable-thread-local/issues/399。
2.new DefaultFileRegion(fileChannel, 0, length) 零拷贝，待了解
