package com.news.bot.wf.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FissureTask{
    private String id;
    private long groupId;
    private long userId;
    private String node;
    private String tier;
    private String missionType;
    private Boolean isHard;
    private Boolean isPersist;
    private Boolean isStorm;
}