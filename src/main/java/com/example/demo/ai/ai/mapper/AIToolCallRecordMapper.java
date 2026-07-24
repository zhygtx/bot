package com.example.demo.ai.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.ai.ai.pojo.entity.AIToolCallRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工具调用记录表的 MyBatis-Plus Mapper。
 */
@Mapper
public interface AIToolCallRecordMapper extends BaseMapper<AIToolCallRecord> {
}
