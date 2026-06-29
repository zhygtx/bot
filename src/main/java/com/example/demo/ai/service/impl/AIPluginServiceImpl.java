package com.example.demo.ai.service.impl;

import com.example.demo.ai.pojo.dto.*;
import com.example.demo.ai.pojo.entity.AIConversationTurn;
import com.example.demo.ai.service.AIPluginService;
import com.example.demo.ai.util.ByteArrayMultipartFile;
import com.example.demo.ai.util.CodeGenerator;
import com.example.demo.ai.util.PluginCompiler;
import com.example.demo.mapper.AIConversationTurnMapper;
import com.example.demo.pojo.entity.plugin.*;
import com.example.demo.service.PluginService;
import com.example.demo.util.PluginUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AI 插件服务实现：纯业务逻辑，不处理 HTTP 层关注点。
 */
@Slf4j
@Service
public class AIPluginServiceImpl implements AIPluginService {

    private final CodeGenerator codeGenerator;
    private final PluginCompiler pluginCompiler;
    private final PluginUtil pluginUtil;
    private final PluginService pluginService;
    private final AIConversationTurnMapper conversationTurnMapper;
    private final ObjectMapper objectMapper;

    public AIPluginServiceImpl(CodeGenerator codeGenerator,
                                PluginCompiler pluginCompiler,
                                PluginUtil pluginUtil,
                                PluginService pluginService,
                                AIConversationTurnMapper conversationTurnMapper,
                                ObjectMapper objectMapper) {
        this.codeGenerator = codeGenerator;
        this.pluginCompiler = pluginCompiler;
        this.pluginUtil = pluginUtil;
        this.pluginService = pluginService;
        this.conversationTurnMapper = conversationTurnMapper;
        this.objectMapper = objectMapper;
    }

    // ==================== AI 代码生成 ====================

    @Override
    @Transactional
    public GenerateResponse startConversation(GenerateRequest request, String userId) {
        String conversationId = UUID.randomUUID().toString();

        // 保存用户消息
        AIConversationTurn userTurn = new AIConversationTurn();
        userTurn.setId(UUID.randomUUID().toString());
        userTurn.setConversationId(conversationId);
        userTurn.setPluginId(request.getPluginId());
        userTurn.setUserId(userId);
        userTurn.setRound(1);
        userTurn.setRole("user");
        userTurn.setContent(request.getRequirements());
        userTurn.setStatus("draft");
        userTurn.setCreateTime(LocalDateTime.now());
        conversationTurnMapper.insert(userTurn);

        // 调用 AI
        CodeGenerator.GenerateResult aiResult;
        if (request.getPluginId() != null) {
            String existingCode = loadPublishedCode(request.getPluginId());
            aiResult = codeGenerator.generateCode(
                    request.getRequirements(), request.getEntityPackage(),
                    request.getMethodPackage(), existingCode);
        } else {
            aiResult = codeGenerator.generateCode(
                    request.getRequirements(), request.getEntityPackage(),
                    request.getMethodPackage());
        }

        if (!aiResult.isSuccess()) {
            throw new RuntimeException("AI 代码生成失败: " + aiResult.getErrorMessage());
        }

        // 保存 AI 响应
        saveAssistantTurn(conversationId, userId, 1,
                aiResult.getFiles(), aiResult.getRawResponse());

        log.info("AI 代码生成成功: conversationId={}, round=1, files={}",
                conversationId, aiResult.getFiles().size());

        return GenerateResponse.builder()
                .conversationId(conversationId)
                .round(1)
                .files(aiResult.getFiles())
                .rawResponse(aiResult.getRawResponse())
                .build();
    }

    @Override
    @Transactional
    public GenerateResponse createTurn(TurnRequest request, String userId) {

        // 计算下一轮次编号
        Integer maxRound = conversationTurnMapper.selectMaxRound(request.getConversationId());
        int nextRound = (maxRound != null ? maxRound : 0) + 1;

        // 保存用户微调指令
        AIConversationTurn userTurn = new AIConversationTurn();
        userTurn.setId(UUID.randomUUID().toString());
        userTurn.setConversationId(request.getConversationId());
        userTurn.setUserId(userId);
        userTurn.setRound(nextRound);
        userTurn.setRole("user");
        userTurn.setContent(request.getInstruction());
        userTurn.setStatus("draft");
        userTurn.setCreateTime(LocalDateTime.now());
        conversationTurnMapper.insert(userTurn);

        // 获取上一轮 AI 代码作为上下文
        String existingCode = loadCurrentAiCode(request.getConversationId());

        // 调用 AI
        CodeGenerator.GenerateResult aiResult = codeGenerator.generateCode(
                request.getInstruction(), request.getEntityPackage(),
                request.getMethodPackage(), existingCode);

        if (!aiResult.isSuccess()) {
            throw new RuntimeException("AI 代码生成失败: " + aiResult.getErrorMessage());
        }

        // 将上一轮 current 标记转为 draft
        if (maxRound != null) {
            conversationTurnMapper.updateStatusToDraftFromRound(request.getConversationId(), maxRound);
        }

        // 保存 AI 响应
        saveAssistantTurn(request.getConversationId(), userId, nextRound,
                aiResult.getFiles(), aiResult.getRawResponse());

        log.info("AI 微调成功: conversationId={}, round={}, files={}",
                request.getConversationId(), nextRound, aiResult.getFiles().size());

        return GenerateResponse.builder()
                .conversationId(request.getConversationId())
                .round(nextRound)
                .files(aiResult.getFiles())
                .rawResponse(aiResult.getRawResponse())
                .build();
    }

    @Override
    @Transactional
    public GenerateResponse undoToRound(String conversationId, int targetRound, String userId) {

        // 查询目标轮次的 AI 代码
        List<AIConversationTurn> targetTurns = conversationTurnMapper
                .selectByConversationIdAndRound(conversationId, targetRound);
        List<AIConversationTurn> assistantTurns = targetTurns.stream()
                .filter(t -> "assistant".equals(t.getRole()))
                .toList();

        if (assistantTurns.isEmpty()) {
            throw new IllegalArgumentException("目标轮次不存在 AI 生成的代码");
        }

        // 删除目标轮次之后的所有记录
        conversationTurnMapper.deleteFromRound(conversationId, targetRound + 1);

        // 将目标轮次的 assistant 记录设为 current
        for (AIConversationTurn turn : assistantTurns) {
            conversationTurnMapper.updateStatus(turn.getId(), "current", turn.getPluginId());
        }

        List<SourceFile> files = deserializeCodeFiles(assistantTurns.get(0).getContent());

        log.info("撤销成功: conversationId={}, targetRound={}", conversationId, targetRound);

        return GenerateResponse.builder()
                .conversationId(conversationId)
                .round(targetRound)
                .files(files)
                .build();
    }

    @Override
    @Transactional
    public ConversationResponse loadConversation(String conversationId) {
        List<AIConversationTurn> turns = conversationTurnMapper.selectByConversationId(conversationId);
        if (turns.isEmpty()) {
            throw new IllegalArgumentException("会话不存在");
        }

        int currentRound = turns.stream()
                .filter(t -> "current".equals(t.getStatus()))
                .map(AIConversationTurn::getRound)
                .max(Integer::compareTo)
                .orElse(0);

        String pluginId = turns.stream()
                .map(AIConversationTurn::getPluginId)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);

        List<ConversationTurnDto> history = turns.stream()
                .map(t -> new ConversationTurnDto(
                        t.getRound(), t.getRole(), t.getContent(), t.getCreateTime()))
                .toList();

        return ConversationResponse.builder()
                .conversationId(conversationId)
                .pluginId(pluginId)
                .currentRound(currentRound)
                .turns(history)
                .build();
    }

    // ==================== 编译上传 ====================

    @Override
    @Transactional
    public Map<String, String> compileAndUpload(PluginCompileRequest request, String userId) {
        // 1. 编译
        PluginCompiler.CompileResult compileResult = pluginCompiler.compile(request.getFiles());
        if (!compileResult.isSuccess()) {
            throw new RuntimeException("编译失败: " + compileResult.getErrors());
        }

        // 读取 JAR
        byte[] jarBytes;
        try {
            jarBytes = Files.readAllBytes(Path.of(compileResult.getJarPath()));
        } catch (Exception e) {
            pluginCompiler.cleanup(compileResult);
            throw new RuntimeException("读取编译产物失败", e);
        }

        // 2. 校验 JAR
        validateJar(compileResult.getJarPath(),
                request.getEntityPackage(), request.getMethodPackage());

        // 3. 上传并获取 pluginId/versionId
        String pluginId = UUID.randomUUID().toString();
        String versionId;
        try {
            versionId = doUpload(request, compileResult, jarBytes, userId, pluginId);

            // 4. 更新 DB 状态
            if (request.getConversationId() != null) {
                List<AIConversationTurn> currentTurns = conversationTurnMapper
                        .selectCurrentByConversationId(request.getConversationId());
                for (AIConversationTurn turn : currentTurns) {
                    conversationTurnMapper.updateStatus(turn.getId(), "published", pluginId);
                }
                conversationTurnMapper.updateStatusToDraftFromRound(
                        request.getConversationId(), 1);
            }

            return Map.of("pluginId", pluginId, "versionId", versionId);

        } finally {
            pluginCompiler.cleanup(compileResult);
        }
    }

    // ==================== 内部方法 ====================

    private void saveAssistantTurn(String conversationId, String userId,
                                    int round, List<SourceFile> files, String rawResponse) {
        String contentJson = serializeCodeFiles(files, rawResponse);
        AIConversationTurn turn = new AIConversationTurn();
        turn.setId(UUID.randomUUID().toString());
        turn.setConversationId(conversationId);
        turn.setUserId(userId);
        turn.setRound(round);
        turn.setRole("assistant");
        turn.setContent(contentJson);
        turn.setStatus("current");
        turn.setCreateTime(LocalDateTime.now());
        conversationTurnMapper.insert(turn);
    }

    private String serializeCodeFiles(List<SourceFile> files, String rawResponse) {
        try {
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("files", files);
            wrapper.put("rawResponse", rawResponse);
            return objectMapper.writeValueAsString(wrapper);
        } catch (JsonProcessingException e) {
            log.error("序列化代码文件失败", e);
            return "[]";
        }
    }

    private List<SourceFile> deserializeCodeFiles(String contentJson) {
        try {
            if (contentJson.trim().startsWith("{")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.readValue(contentJson, Map.class);
                Object filesObj = map.get("files");
                if (filesObj instanceof List) {
                    return objectMapper.convertValue(filesObj, objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, SourceFile.class));
                }
            }
            return objectMapper.readValue(contentJson, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, SourceFile.class));
        } catch (Exception e) {
            log.warn("反序列化代码文件失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String loadCurrentAiCode(String conversationId) {
        List<AIConversationTurn> currentTurns =
                conversationTurnMapper.selectCurrentByConversationId(conversationId);
        if (currentTurns.isEmpty()) {
            return null;
        }
        return currentTurns.get(currentTurns.size() - 1).getContent();
    }

    private String loadPublishedCode(String pluginId) {
        AIConversationTurn published = conversationTurnMapper.selectPublishedByPluginId(pluginId);
        if (published == null) {
            log.warn("插件 {} 无已发布代码记录，降级为从零生成", pluginId);
            return null;
        }
        return published.getContent();
    }

    private void validateJar(String jarPath, String entityPackage, String methodPackage) {
        PluginVersion tempVersion = new PluginVersion();
        tempVersion.setId(UUID.randomUUID().toString());
        tempVersion.setPath(jarPath);
        tempVersion.setEntityPackage(entityPackage);
        tempVersion.setMethodPackage(methodPackage);

        List<EntityInfo> entityInfoList = pluginUtil.scanEntities(tempVersion);
        List<MethodClassInfo> methodClassInfoList = pluginUtil.scanMethodClasses(tempVersion);

        List<ParameterInfo> parameterInfoList = methodClassInfoList.stream()
                .map(MethodClassInfo::getMethods)
                .flatMap(List::stream)
                .map(MethodInfo::getParameters)
                .flatMap(List::stream)
                .toList();

        for (ParameterInfo param : parameterInfoList) {
            if (!param.isValidType()) {
                throw new IllegalArgumentException("参数校验失败: " + param.getInvalidTypeMessage());
            }
        }
        for (ParameterInfo param : parameterInfoList) {
            if (!param.isValidNullable()) {
                throw new IllegalArgumentException("参数校验失败: " + param.getInvalidNullableMessage());
            }
        }

        log.info("JAR 校验通过，实体类 {} 个，方法类 {} 个",
                entityInfoList.size(), methodClassInfoList.size());
    }

    /**
     * 上传插件
     */
    private String doUpload(PluginCompileRequest request,
                             PluginCompiler.CompileResult compileResult,
                             byte[] jarBytes, String userId, String pluginId) {
        PluginInfo pluginInfo = new PluginInfo();
        pluginInfo.setId(pluginId);
        pluginInfo.setName(request.getName());
        pluginInfo.setDescription(request.getDescription());
        pluginInfo.setAuthorId(userId);
        pluginInfo.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        pluginInfo.setVersionCount(0);

        String versionId = UUID.randomUUID().toString();
        PluginVersion pluginVersion = new PluginVersion();
        pluginVersion.setId(versionId);
        pluginVersion.setVersion(request.getVersion() != null ? request.getVersion() : "1.0.0");
        pluginVersion.setChangelog(request.getChangelog());
        pluginVersion.setEntityPackage(request.getEntityPackage());
        pluginVersion.setMethodPackage(request.getMethodPackage());

        pluginInfo.setPluginVersionList(Collections.singletonList(pluginVersion));

        ByteArrayMultipartFile multipartFile = new ByteArrayMultipartFile(
                "file", request.getName() + "-" + pluginVersion.getVersion() + ".jar", jarBytes);

        pluginService.add(pluginInfo, multipartFile);

        log.info("插件上传成功: name={}, pluginId={}, versionId={}",
                request.getName(), pluginId, versionId);
        return versionId;
    }
}
