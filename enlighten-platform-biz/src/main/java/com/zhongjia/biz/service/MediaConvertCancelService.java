package com.zhongjia.biz.service;

/**
 * 可打断任务的取消标记服务（分布式）。
 * 使用共享存储（Redis）记录取消标记，支持集群消费端一致感知。
 */
public interface MediaConvertCancelService {
    /** 设置任务为已取消（打断）。返回true表示设置成功或已处于取消状态。 */
    boolean cancel(Long recordV2Id);

    /** 查询任务是否被取消（打断）。 */
    boolean isCancelled(Long recordV2Id);
}


