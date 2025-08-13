package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.Draft;
import com.zhongjia.biz.mapper.DraftMapper;
import com.zhongjia.biz.service.DraftService;
import org.springframework.stereotype.Service;

@Service
public class DraftServiceImpl extends ServiceImpl<DraftMapper, Draft> implements DraftService {
}



