package com.news.bot.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupPermission {
    private long id;
    private long groupId;
    private String permission;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime overTime;
}
