package com.news.bot.wf.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true) // 启用链式调用
public class Fissure {

    private String id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private ZonedDateTime activation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private ZonedDateTime expiry;

    private String node;

    private String missionType;

    private String missionTypeKey;

    private String enemy;

    private String enemyKey;

    private String nodeKey;

    private String tier;

    private Integer tierNum;

    private String eta;

    private Boolean isStorm;

    private Boolean isHard;
}