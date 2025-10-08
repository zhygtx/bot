package com.news.bot.wf.service.impl;

import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.news.bot.wf.mapper.FissureMapper;
import com.news.bot.wf.pojo.Fissure;
import com.news.bot.wf.pojo.FissureTask;
import com.news.bot.wf.service.FissureService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FissureServiceImpl implements FissureService {


    @Autowired
    private FissureMapper fissureMapper;

    @PostConstruct
    @PreDestroy
    public void clearFissures() {
        fissureMapper.clearFissures();
    }

    @Override
    public void batchInsertFissures(List<Fissure> fissures) {
        fissureMapper.batchInsert(fissures);
    }

    @Override
    public List<String> getActiveIds() {
        return fissureMapper.getActiveIds();
    }

    @Override
    public List<Fissure> selectByKey(String key) {
        return fissureMapper.selectByKey(key);
    }

    @Override
    public void fissureStatistic(List<Fissure> fissures) {
        fissureMapper.fissureStatistic(fissures);
    }

    @Override
    public void insertFissureTask(FissureTask fissureTask) {
        fissureMapper.insertFissureTask(fissureTask);
    }

    @Override
    public int deleteFissureTask(FissureTask fissureTask) {
        return fissureMapper.deleteFissureTask(fissureTask);
    }

    @Override
    public List<FissureTask> selectFissureTask(GroupMessageEvent event) {
        return fissureMapper.selectFissureTaskByUserOrGroup(event);
    }

    @Override
    public List<FissureTask> selectAllFissureTasks() {
        return fissureMapper.selectAllFissureTasks();
    }

    @Override
    public String selectTemplateName(Long groupId, Long userId) {
        return fissureMapper.selectTemplateName(groupId, userId);
    }
}