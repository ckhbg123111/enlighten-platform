package com.zhongjia.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhongjia.biz.entity.Folder;

import java.util.List;

public interface FolderService {
    Long create(Long userId, String name, Integer sort);
    boolean rename(Long userId, Long folderId, String newName);
    boolean deleteSoft(Long userId, Long folderId);
    List<Folder> listByUser(Long userId);
    boolean updateOrders(Long userId, List<Long> folderIdsInOrder);
}


