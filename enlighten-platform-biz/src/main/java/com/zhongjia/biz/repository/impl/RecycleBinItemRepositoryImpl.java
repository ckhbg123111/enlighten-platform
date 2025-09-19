package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.RecycleBinItem;
import com.zhongjia.biz.mapper.RecycleBinItemMapper;
import com.zhongjia.biz.repository.RecycleBinItemRepository;
import org.springframework.stereotype.Repository;

@Repository
public class RecycleBinItemRepositoryImpl extends ServiceImpl<RecycleBinItemMapper, RecycleBinItem> implements RecycleBinItemRepository {
}


