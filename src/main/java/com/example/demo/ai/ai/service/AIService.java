package com.example.demo.ai.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.ai.ai.pojo.dto.AIChatMessageDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

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
     * 流式过程中通过 AIUtil.sendSseEvent(emitter, ...) 推送增量事件，
     * 流式完成后由本方法推送 done 事件，调用方仅需处理 emitter.complete()。
     *
     * @param message        用户指令文本
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param emitter        SSE emitter，由本方法直接消费
     */
    void aiGenerate(String message, String conversationId, String userId,
                    SseEmitter emitter) throws IOException;

    /**
     * 编译代码。
     * @param conversationId 会话 ID
     * @param emitter        SSE emitter，由本方法直接消费
     */
    void compileCode(String conversationId, SseEmitter emitter) throws Exception;

    /**
     * 撤销指定轮次的对话记录，同时删除关联的代码文件和工具调用记录。
     */
    Boolean undo(String conversationId, Integer round);
}
