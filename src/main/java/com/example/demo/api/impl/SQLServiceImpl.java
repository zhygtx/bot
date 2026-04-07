package com.example.demo.api.impl;

import com.example.demo.mapper.workflow.PluginDataMapper;
import com.example.demo.util.ThreadLocalManager;
import com.github.zhygtx.pojo.PluginData;
import com.github.zhygtx.service.SQLService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SQLServiceImpl implements SQLService {

    private final PluginDataMapper pluginDataMapper;

    public SQLServiceImpl(PluginDataMapper pluginDataMapper) {
        this.pluginDataMapper = pluginDataMapper;
    }

    @Override
    @Transactional
    public int insert(String data) {
        return pluginDataMapper.insertByData(data, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int insert(String index, String data) {
        return pluginDataMapper.insertByIndexData(index, data, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int insert(List<String> data) {
        return pluginDataMapper.insertByList(null, data, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int insert(String index, List<String> data) {
        return pluginDataMapper.insertByList(index, data, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int insert(Map<String, String> data) {
        return pluginDataMapper.insertByMap(data, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int insertByIndexMap(Map<String, List<String>> data) {
        return pluginDataMapper.insertByMapList(data, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int delete() {
        return pluginDataMapper.delete(ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int delete(Integer id) {
        return pluginDataMapper.deleteById(id);
    }

    @Override
    @Transactional
    public int delete(String index) {
        return pluginDataMapper.deleteByIndex(index, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int delete(List<String> index) {
        return pluginDataMapper.deleteByIndexList(index, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int deleteByIds(List<Integer> ids) {
        return pluginDataMapper.deleteByIds(ids);
    }

    @Override
    @Transactional
    public int update(Integer id, String data) {
        return pluginDataMapper.updateById(id, data);
    }

    @Override
    @Transactional
    public int update(String index, String data) {
        return pluginDataMapper.updateByIndex(index, data, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    public List<PluginData> select() {
        return pluginDataMapper.selectAll(ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    public PluginData select(Integer id) {
        return pluginDataMapper.selectById(id);
    }

    @Override
    public List<PluginData> select(String index) {
        return pluginDataMapper.selectByIndex(index, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }
}
