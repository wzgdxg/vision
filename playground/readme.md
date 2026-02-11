# 如何优雅关闭异常连接，不报异常


# 总结
1. 不要让回调阻塞了 Netty 的 EventLoop 线程，导致请求和业务逻辑都堵塞。
2. ByteBuf的markReaderIndex和resetReaderIndex方法特别有用，mark后不管buf被当做参数传递到哪，调用reset后都可以恢复到之前的状态。  
   markWriterIndex和resetWriterIndex同理。
3. 