package com.generalbot.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.generalbot.ai.dto.AIPluginListDto;
import com.generalbot.ai.entity.AIChatMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AIService extends IService<AIChatMessage> {

    /**
     * 分页查询会话消息：初始加载最新一页，beforeRound 向上翻页。
     * @param conversationId 会话 ID
     * @param beforeRound    只返回轮次小于该值的消息，null 表示加载最新页
     * @param pageSize       每页条数
     * @return 包含 messages（按轮次升序）与 hasMore 的 Map
     */
    Map<String, Object> findPageByConversationId(String conversationId, Integer beforeRound, Integer pageSize);

    /**
     * 根据用户 ID 查询所有消息记录。
     * @param userId 用户 ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 排序后的所有消息记录列表
     */
    List<AIPluginListDto> findPluginList(String userId, Integer pageNum, Integer pageSize);

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
     * @param messageId      消息 ID
     * @param emitter        SSE emitter，由本方法直接消费
     */
    void compileCode(String messageId, SseEmitter emitter) throws Exception;

    /**
     * 撤销指定轮次的对话记录，同时删除关联的代码文件和工具调用记录。
     */
    Boolean undo(String conversationId, Integer round);
}
