package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.Folder;
import com.zhongjia.biz.mapper.FolderMapper;
import com.zhongjia.biz.repository.FolderRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FolderRepositoryImpl extends ServiceImpl<FolderMapper, Folder> implements FolderRepository {
}


