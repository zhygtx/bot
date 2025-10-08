package com.news.bot.service;

import com.news.bot.pojo.GroupPermission;

public interface GroupPermissionService {

    void insert(GroupPermission groupPermission);

    void delete(GroupPermission groupPermission);

    boolean notHasPermission(long groupId, String permission);
}
