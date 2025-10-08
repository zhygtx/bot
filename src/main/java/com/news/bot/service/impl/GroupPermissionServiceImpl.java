package com.news.bot.service.impl;

import com.news.bot.mapper.GroupPermissionMapper;
import com.news.bot.pojo.GroupPermission;
import com.news.bot.service.GroupPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupPermissionServiceImpl implements GroupPermissionService {

    @Autowired
    private GroupPermissionMapper groupPermissionMapper;

    public void insert(GroupPermission groupPermission) {
        groupPermissionMapper.insert(groupPermission);
    }

    public void delete(GroupPermission groupPermission) {
        groupPermissionMapper.delete(groupPermission);
    }

    public boolean notHasPermission(long groupId, String permission) {
        return !groupPermissionMapper.hasPermission(groupId, permission);
    }
}
