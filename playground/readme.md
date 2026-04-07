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




# 开发经验
1. jdk17下，如果报错ClassCircularityError: java/lang/WeakPairMap$Pair$Weak，原因不是程序问题，是jdk问题https://github.com/alibaba/transmittable-thread-local/issues/399。

