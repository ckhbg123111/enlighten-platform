package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhongjia.biz.entity.Folder;
import com.zhongjia.biz.entity.GzhArticle;
import com.zhongjia.biz.repository.FolderRepository;
import com.zhongjia.biz.repository.GzhArticleRepository;
import com.zhongjia.biz.service.FolderService;
import com.zhongjia.biz.service.GzhArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderServiceImpl implements FolderService {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private GzhArticleRepository gzhArticleRepository;
    @Autowired
    private GzhArticleService gzhArticleService;

    @Override
    public Long create(Long userId, String name, Integer sort) {
        String uniqueName = allocateUniqueName(userId, name, null);
        Folder folder = new Folder()
                .setUserId(userId)
                .setName(uniqueName)
                .setSort(sort == null ? 0 : sort);
        boolean ok = folderRepository.save(folder);
        return ok ? folder.getId() : null;
    }

    @Override
    public boolean rename(Long userId, Long folderId, String newName) {
        Folder exist = folderRepository.getById(folderId);
        if (exist == null || !exist.getUserId().equals(userId)) {
            return false;
        }
        // 如果重名，则自动追加（1）（2）…
        String uniqueName = allocateUniqueName(userId, newName, folderId);
        Folder upd = new Folder().setId(folderId).setName(uniqueName);
        return folderRepository.updateById(upd);
    }

    @Override
    public boolean deleteSoft(Long userId, Long folderId) {
        Folder exist = folderRepository.getById(folderId);
        if (exist == null || !exist.getUserId().equals(userId)) {
            return false;
        }
        // 软删除文件夹
        folderRepository.update(new LambdaUpdateWrapper<Folder>()
                .eq(Folder::getId, folderId)
                .eq(Folder::getUserId, userId)
                .set(Folder::getDeleted, 1)
                .set(Folder::getDeleteTime, java.time.LocalDateTime.now()));

        // 同时软删除该文件夹下的文章并放入回收站
        java.util.List<GzhArticle> list = gzhArticleRepository.list(new LambdaQueryWrapper<GzhArticle>()
                .eq(GzhArticle::getUserId, userId)
                .eq(GzhArticle::getFolderId, folderId)
                .eq(GzhArticle::getDeleted, 0));
        if (!list.isEmpty()) {
            java.util.List<Long> ids = list.stream().map(GzhArticle::getId).toList();
            // 批量软删（进入回收站）
            gzhArticleService.batchSoftDelete(userId, ids);
        }
        return true;
    }

    @Override
    public List<Folder> listByUser(Long userId) {
        return folderRepository.list(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getUserId, userId)
                .eq(Folder::getDeleted, 0)
                .orderByAsc(Folder::getSort)
                .orderByAsc(Folder::getId));
    }

    @Override
    public boolean updateOrders(Long userId, List<Long> folderIdsInOrder) {
        int sort = 0;
        for (Long folderId : folderIdsInOrder) {
            folderRepository.update(new LambdaUpdateWrapper<Folder>()
                    .eq(Folder::getId, folderId)
                    .eq(Folder::getUserId, userId)
                    .set(Folder::getSort, sort++));
        }
        return true;
    }

    private boolean nameExists(Long userId, String name, Long excludeId) {
        LambdaQueryWrapper<Folder> w = new LambdaQueryWrapper<Folder>()
                .eq(Folder::getUserId, userId)
                .eq(Folder::getDeleted, 0)
                .eq(Folder::getName, name);
        if (excludeId != null) {
            w.ne(Folder::getId, excludeId);
        }
        return folderRepository.count(w) > 0;
    }

    private String allocateUniqueName(Long userId, String desiredName, Long excludeId) {
        if (desiredName == null || desiredName.isEmpty()) {
            desiredName = "未命名文件夹";
        }
        if (!nameExists(userId, desiredName, excludeId)) {
            return desiredName;
        }
        int i = 1;
        while (true) {
            String candidate = desiredName + "(" + i + ")";
            if (!nameExists(userId, candidate, excludeId)) {
                return candidate;
            }
            i++;
        }
    }
}


