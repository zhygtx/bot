package com.news.bot.wf.service;

import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.news.bot.wf.pojo.Fissure;
import com.news.bot.wf.pojo.FissureTask;
import java.util.List;

public interface FissureService {

    void clearFissures();

    void batchInsertFissures(List<Fissure> fissures);

    List<String> getActiveIds();

    List<Fissure> selectByKey(String key);

    void fissureStatistic(List<Fissure> fissures);

    void insertFissureTask(FissureTask fissureTask);

    int deleteFissureTask(FissureTask fissureTask);

    List<FissureTask> selectFissureTask(GroupMessageEvent event);

    List<FissureTask> selectAllFissureTasks();

    String selectTemplateName(Long groupId,Long userId);
}