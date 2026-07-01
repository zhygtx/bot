package com.example.demo.ai.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.ai.ai.pojo.dto.AIChatMessageDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AIChatMessageMapper extends BaseMapper<AIChatMessage> {

    /**
     * 根据用户 ID 查询所有消息记录。
     * @param userId 用户 ID
     * @param offset 偏移量（从 0 开始）
     * @param count  查询数量
     * @return 消息记录列表
     */
    List<AIChatMessageDto> selectByUserId(@Param("userId") String userId, @Param("offset") Integer offset, @Param("count") Integer count);

}
