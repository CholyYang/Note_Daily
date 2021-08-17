# StateBackend
- flink checkpoint 状态保存时选择 stateBackend, 持久化存储状态的方式和位置
    + MemoryStateBackend 开发调试，对数据丢失重复不敏感
    + FsStateBackend 普通状态，大窗口，KV结构
    + RocksDBStateBackend 超大状态，超大窗口，大型KV结构

- FsStateBackend 和 MemoryStateBackend 吞吐量差距不大，都是使用 TM 堆内存管理，RocksDBStateBackend 吞吐量较低

- MemoryStateBackend
    + 可以异步存储快照，避免主流程堵塞，默认关闭
    + 最大单个状态最大默认不能超过一个akka帧，5M
    + 聚合状态需能在jobManager内存中存放下

- FsStateBackend
    + 使用前，需配置状态存储的文件系统路径 (如HDFS路径)
    + 状态会被存到TaskManager内存中，所以状态大小不能超过TM内存，避免OOM
    + 默认异步生成快照

- RocksDBBackend
    + 通过RocksDB存储到本地文件系统，RocksDB先将数据放在内存中，快写满时写入磁盘，仅支持异步快照
    + RocksDB 支持增量CheckPoint,目前唯一增量的Checkpoint backend，只需要上传增量的sst到目录
    + State 大小不能超过TM的内存+磁盘，单Key最大2G
    + 适合高可用，大状态，增量，性能要求不高
  
- 忽略删除节点无法恢复checkpoint, 参数: allowNonRestoreState

# Exactly once （End to End）
- Flink 借助`分布式快照 + 两阶段提交`实现
- Spark 是对Driver端的故障恢复checkpoint, Flink 快照保存算子级别，并对全局数据做快照
- CheckPoint 核心：Barrier 栅栏
  + Barrier会随数据往下游算子流动，并在经过算子时添加标记，当所有sink的算子都标记后，会通知协调者snapshot完成
- TwoPhaseCommitSinkFunction 两阶段提交
  + beginTransaction 创建临时目录
  + preCommit 结束写临时文件
  + commit 移动临时文件到指定目录
  + abort 删除临时文件