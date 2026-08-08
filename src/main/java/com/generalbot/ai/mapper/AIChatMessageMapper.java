package com.generalbot.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.generalbot.ai.dto.AIPluginListDto;
import com.generalbot.ai.entity.AIChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AIChatMessageMapper extends BaseMapper<AIChatMessage> {

    /**
     * 根据用户 ID 分页查询 AI 插件会话列表。
     * @param userId 用户 ID
     * @param offset 偏移量（从 0 开始）
     * @param count  查询数量
     * @return 插件会话列表 DTO
     */
    List<AIPluginListDto> selectPluginListByUserId(@Param("userId") String userId, @Param("offset") Integer offset, @Param("count") Integer count);

}
