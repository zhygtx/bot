package com.generalbot.plugin.runtime;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.generalbot.plugin.mapper.PluginDataMapper;
import com.generalbot.common.context.ThreadLocalManager;
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
        return pluginDataMapper.deleteByUserIdAndPluginId(ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int delete(Integer id) {
        return pluginDataMapper.deleteByIdWithInteger(id);
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
        return pluginDataMapper.updateByIdWithInteger(id, data);
    }

    @Override
    @Transactional
    public int update(String index, String data) {
        return pluginDataMapper.updateByIndex(index, data, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    @Transactional
    public int update(List<PluginData> pluginDataList){
        if (pluginDataList == null || pluginDataList.isEmpty()) {
            return 0;
        }
        return pluginDataMapper.updateBatchById(pluginDataList);
    }

    @Override
    public List<PluginData> select() {
        return pluginDataMapper.selectAll(ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    public PluginData selectById(Integer id) {
        return pluginDataMapper.selectByIdWithInteger(id);
    }

    @Override
    public PluginData selectByIndex(String index) {
        List<PluginData> list = pluginDataMapper.selectByIndex(index, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    @Override
    public List<PluginData> select(String index) {
        return pluginDataMapper.selectByIndex(index, ThreadLocalManager.getUserId(), ThreadLocalManager.getPluginId());
    }

    @Override
    public List<PluginData> selectList(Wrapper<PluginData> wrapper) {
        Wrapper<PluginData> wrappedWrapper = addScopeToWrapper(wrapper);
        return pluginDataMapper.selectList(wrappedWrapper);
    }

    @Override
    public PluginData selectOne(Wrapper<PluginData> wrapper) {
        Wrapper<PluginData> wrappedWrapper = addScopeToWrapper(wrapper);
        return pluginDataMapper.selectOne(wrappedWrapper);
    }

    @Override
    public int selectCount(Wrapper<PluginData> wrapper) {
        Wrapper<PluginData> wrappedWrapper = addScopeToWrapper(wrapper);
        return Math.toIntExact(pluginDataMapper.selectCount(wrappedWrapper));
    }

    @Override
    public IPage<PluginData> selectPage(Page<PluginData> page, Wrapper<PluginData> wrapper) {
        Wrapper<PluginData> wrappedWrapper = addScopeToWrapper(wrapper);
        return pluginDataMapper.selectPage(page, wrappedWrapper);
    }

    @Override
    @Transactional
    public int update(Wrapper<PluginData> wrapper, PluginData entity) {
        Wrapper<PluginData> wrappedWrapper = addScopeToWrapper(wrapper);
        return pluginDataMapper.update(entity, wrappedWrapper);
    }

    @Override
    @Transactional
    public int delete(Wrapper<PluginData> wrapper) {
        Wrapper<PluginData> wrappedWrapper = addScopeToWrapper(wrapper);
        return pluginDataMapper.delete(wrappedWrapper);
    }


    @SuppressWarnings("unchecked")
    private <T extends Wrapper<PluginData>> T addScopeToWrapper(T wrapper) {
        if (wrapper instanceof LambdaQueryWrapper) {
            LambdaQueryWrapper<PluginData> lambdaWrapper = (LambdaQueryWrapper<PluginData>) wrapper;
            return (T) lambdaWrapper.eq(PluginData::getUserId, ThreadLocalManager.getUserId())
                                     .eq(PluginData::getPluginId, ThreadLocalManager.getPluginId());
        } else if (wrapper instanceof LambdaUpdateWrapper) {
            LambdaUpdateWrapper<PluginData> lambdaWrapper = (LambdaUpdateWrapper<PluginData>) wrapper;
            return (T) lambdaWrapper.eq(PluginData::getUserId, ThreadLocalManager.getUserId())
                                     .eq(PluginData::getPluginId, ThreadLocalManager.getPluginId());
        } else if (wrapper instanceof QueryWrapper) {
            QueryWrapper<PluginData> queryWrapper = (QueryWrapper<PluginData>) wrapper;
            return (T) queryWrapper.eq("user_id", ThreadLocalManager.getUserId())
                                     .eq("plugin_id", ThreadLocalManager.getPluginId());
        } else if (wrapper instanceof UpdateWrapper) {
            UpdateWrapper<PluginData> updateWrapper = (UpdateWrapper<PluginData>) wrapper;
            return (T) updateWrapper.eq("user_id", ThreadLocalManager.getUserId())
                                     .eq("plugin_id", ThreadLocalManager.getPluginId());
        }
        return wrapper;
    }
}
