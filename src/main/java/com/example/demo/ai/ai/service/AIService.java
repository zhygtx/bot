package com.example.demo.ai.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.ai.ai.pojo.dto.AIChatMessageDto;
import com.example.demo.ai.ai.pojo.dto.CompileCodeDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public interface AIService extends IService<AIChatMessage> {

    /**
     * 根据会话 ID 查询所有消息记录。
     * @param conversationId 会话 ID
     * @return 排序后的所有消息记录列表
     */
    List<AIChatMessage> findByConversationId(String conversationId);

    /**
     * 根据用户 ID 查询所有消息记录。
     * @param userId 用户 ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 排序后的所有消息记录列表
     */
    List<AIChatMessageDto> findDtoList(String userId, Integer pageNum, Integer pageSize);

    /**
     * 启动流式 AI 生成任务。
     * @param message 用户指令文本
     * @param conversationId 会话 ID
     * @param userId 用户 ID
     * @param onEvent 事件消费函数，第一个参数为事件类型（"delta" / "file_start" / "file_end"），第二个参数为事件数据
     * @return 生成任务 ID
     */
    List<AIChatMessage> aiGenerate(String message, String conversationId, String userId,
                                   BiConsumer<String, Object> onEvent, AtomicBoolean cancelled) throws IOException;

    /**
     * 编译代码。
     * @param compileCodeDto 编译参数-
     * @param onEvent 事件消费函数，第一个参数为事件类型，第二个参数为事件数据
     * @return 插件ID
     */
    String compileCode(CompileCodeDto compileCodeDto, BiConsumer<String, Object> onEvent) throws Exception;
}
