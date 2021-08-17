##基本概念
 - HW是high watermark，高水位，标识一个特定的消息偏移量（offset）,消费者只能拉取到这个offset之前的消息
 - 高水位指向的消息对消费者而言是不可见的
 - LEO是Log End Offset，标识当前日志文件中下一条待写入消息的offset
 - 有ISR为依托，获取其中最大被同步的数据作为可消费数据，即HW
    
##参数配置
 - message.max.bytes
        该参数用来指定broker所能接收消息的最大值，默认值为1000012b，约等于976.6k，如果Producer发送的消息大于这个参数所设置的值，那么Producer就会报出RecordToolLargeException的异常，如果需要修改这个参数，那么需要考虑max.request.size（客户端参数），max.message.bytes(topic端参数)影响
    
##客户端开发
 - 如果key不为null，那么默认的分区器会对key进行hash（采用Murmurhash2算法，具备高运算性能及低碰撞率），最终根据得到的哈希值来计算分区号，拥有相同key的消息会被写入同一个分区。如果key为null,那么消息将会以轮询的方式发往主题内的各个可用分区
    