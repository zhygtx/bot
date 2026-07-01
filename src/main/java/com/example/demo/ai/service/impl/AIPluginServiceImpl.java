package com.example.demo.ai.service.impl;

import com.example.demo.ai.exception.CodeReviewFailedException;
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
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.*;
import java.util.function.Consumer;

/**
 * AI 插件服务实现：纯业务逻辑，不处理 HTTP 层关注点。
 */
@Slf4j
@Service
public class AIPluginServiceImpl implements AIPluginService {

    private static final String DEFAULT_ENTITY_PACKAGE = "com.example.entity";
    private static final String DEFAULT_METHOD_PACKAGE = "com.example.service";

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
    public GenerateResponse createStreamTurn(GenerateRequest startRequest,
                                             TurnRequest turnRequest,
                                             String userId,
                                             Consumer<String> onDelta) {
        if (turnRequest != null && turnRequest.getConversationId() != null
                && !turnRequest.getConversationId().isBlank()) {
            return createStreamExistingTurn(turnRequest, userId, onDelta);
        }
        return createStreamStartTurn(startRequest, userId, onDelta);
    }

    private GenerateResponse createStreamStartTurn(GenerateRequest request,
                                                   String userId,
                                                   Consumer<String> onDelta) {
        String entityPackage = normalizePackage(request.getEntityPackage(), DEFAULT_ENTITY_PACKAGE);
        String methodPackage = normalizePackage(request.getMethodPackage(), DEFAULT_METHOD_PACKAGE);
        String conversationId = UUID.randomUUID().toString();

        saveUserTurn(conversationId, request.getPluginId(), userId, 1, request.getRequirements());

        CodeGenerator.GenerateResult aiResult;
        if (request.getPluginId() != null) {
            String existingCode = loadPublishedCode(request.getPluginId());
            aiResult = codeGenerator.generateCodeStream(
                    Collections.emptyList(),
                    request.getRequirements(), entityPackage, methodPackage, existingCode, onDelta);
        } else {
            aiResult = codeGenerator.generateCodeStream(
                    Collections.emptyList(),
                    request.getRequirements(), entityPackage, methodPackage, null, onDelta);
        }
        if (!aiResult.isSuccess()) {
            throw new RuntimeException("AI 代码生成失败: " + aiResult.getErrorMessage());
        }

        saveAssistantTurn(conversationId, userId, 1,
                aiResult.getFiles(), aiResult.getDependencies(),
                aiResult.getPluginName(), aiResult.getPluginDescription(),
                aiResult.getRawResponse());

        return GenerateResponse.builder()
                .conversationId(conversationId)
                .round(1)
                .files(aiResult.getFiles())
                .dependencies(aiResult.getDependencies())
                .pluginName(aiResult.getPluginName())
                .pluginDescription(aiResult.getPluginDescription())
                .rawResponse(aiResult.getRawResponse())
                .build();
    }

    private GenerateResponse createStreamExistingTurn(TurnRequest request,
                                                      String userId,
                                                      Consumer<String> onDelta) {
        String entityPackage = normalizePackage(request.getEntityPackage(), DEFAULT_ENTITY_PACKAGE);
        String methodPackage = normalizePackage(request.getMethodPackage(), DEFAULT_METHOD_PACKAGE);
        Integer maxRound = conversationTurnMapper.selectMaxRound(request.getConversationId());
        int nextRound = (maxRound != null ? maxRound : 0) + 1;

        List<Message> historyMessages = buildHistoryMessages(request.getConversationId());
        saveUserTurn(request.getConversationId(), null, userId, nextRound, request.getInstruction());

        String existingCode = loadCurrentAiCode(request.getConversationId());
        CodeGenerator.GenerateResult aiResult = codeGenerator.generateCodeStream(
                historyMessages,
                request.getInstruction(), entityPackage, methodPackage, existingCode, onDelta);
        if (!aiResult.isSuccess()) {
            throw new RuntimeException("AI 代码生成失败: " + aiResult.getErrorMessage());
        }
        if (maxRound != null) {
            conversationTurnMapper.updateStatusToDraftFromRound(request.getConversationId(), maxRound);
        }

        saveAssistantTurn(request.getConversationId(), userId, nextRound,
                aiResult.getFiles(), aiResult.getDependencies(),
                aiResult.getPluginName(), aiResult.getPluginDescription(),
                aiResult.getRawResponse());

        return GenerateResponse.builder()
                .conversationId(request.getConversationId())
                .round(nextRound)
                .files(aiResult.getFiles())
                .dependencies(aiResult.getDependencies())
                .pluginName(aiResult.getPluginName())
                .pluginDescription(aiResult.getPluginDescription())
                .rawResponse(aiResult.getRawResponse())
                .build();
    }

    @Override
    @Transactional
    public GenerateResponse deleteFromRound(String conversationId, int round, String userId) {
        if (round <= 0) {
            throw new IllegalArgumentException("删除轮次必须大于 0");
        }

        List<AIConversationTurn> targetTurns = conversationTurnMapper
                .selectByConversationIdAndRound(conversationId, round);
        if (targetTurns.isEmpty()) {
            throw new IllegalArgumentException("目标轮次不存在");
        }
        boolean ownedByUser = targetTurns.stream().anyMatch(t -> userId.equals(t.getUserId()));
        if (!ownedByUser) {
            throw new SecurityException("无权操作该会话");
        }

        conversationTurnMapper.deleteFromRound(conversationId, round);

        int previousRound = round - 1;
        if (previousRound <= 0) {
            return GenerateResponse.builder()
                    .conversationId(conversationId)
                    .round(0)
                    .files(Collections.emptyList())
                    .dependencies(Collections.emptyList())
                    .build();
        }

        List<AIConversationTurn> previousTurns = conversationTurnMapper
                .selectByConversationIdAndRound(conversationId, previousRound);
        List<AIConversationTurn> assistantTurns = previousTurns.stream()
                .filter(t -> "assistant".equals(t.getRole()))
                .toList();
        if (assistantTurns.isEmpty()) {
            return GenerateResponse.builder()
                    .conversationId(conversationId)
                    .round(previousRound)
                    .files(Collections.emptyList())
                    .dependencies(Collections.emptyList())
                    .build();
        }

        for (AIConversationTurn turn : assistantTurns) {
            conversationTurnMapper.updateStatus(turn.getId(), "current", turn.getPluginId());
        }

        AIConversationTurn latestAssistant = assistantTurns.get(assistantTurns.size() - 1);
        return GenerateResponse.builder()
                .conversationId(conversationId)
                .round(previousRound)
                .files(deserializeCodeFiles(latestAssistant.getContent()))
                .dependencies(deserializeDependencies(latestAssistant.getContent()))
                .pluginName(deserializePluginName(latestAssistant.getContent()))
                .pluginDescription(deserializePluginDescription(latestAssistant.getContent()))
                .reviewResult(deserializeReviewResult(latestAssistant.getContent()))
                .rawResponse(deserializeRawResponse(latestAssistant.getContent()))
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
        List<Dependency> dependencies = deserializeDependencies(assistantTurns.get(0).getContent());
        String pluginName = deserializePluginName(assistantTurns.get(0).getContent());
        String pluginDescription = deserializePluginDescription(assistantTurns.get(0).getContent());

        log.info("撤销成功: conversationId={}, targetRound={}", conversationId, targetRound);

        return GenerateResponse.builder()
                .conversationId(conversationId)
                .round(targetRound)
                .files(files)
                .dependencies(dependencies)
                .pluginName(pluginName)
                .pluginDescription(pluginDescription)
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
                .filter(t -> AIConversationTurn.Status.CURRENT.equals(t.getStatus()))
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
    public PluginCompileResponse compileAndUpload(PluginCompileRequest request, String userId) {
        // ========== 0. 编译前按配置审查 ==========
        CodeGenerator.ReviewResult reviewResult = codeGenerator.reviewCode(request.getFiles());
        if (reviewResult != null && !reviewResult.isPassed()) {
            throw new CodeReviewFailedException(
                    "代码审查未通过，请先微调修复问题后再编译", reviewResult);
        }

        // 1. 编译
        List<Dependency> deps = request.getDependencies() != null
                ? request.getDependencies() : Collections.emptyList();
        PluginCompiler.CompileResult compileResult = pluginCompiler.compile(request.getFiles(), deps);
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

            return PluginCompileResponse.builder()
                    .pluginId(pluginId)
                    .versionId(versionId)
                    .build();

        } finally {
            pluginCompiler.cleanup(compileResult);
        }
    }

    // ==================== 内部方法 ====================

    private void saveAssistantTurn(String conversationId, String userId,
                                    int round, List<SourceFile> files,
                                    List<Dependency> dependencies,
                                    String pluginName,
                                    String pluginDescription,
                                    String rawResponse) {
        String contentJson = serializeCodeFiles(files, dependencies, pluginName, pluginDescription, rawResponse);
        AIConversationTurn turn = new AIConversationTurn();
        turn.setId(UUID.randomUUID().toString());
        turn.setConversationId(conversationId);
        turn.setUserId(userId);
        turn.setRound(round);
        turn.setRole("assistant");
        turn.setContent(contentJson);
        turn.setStatus(AIConversationTurn.Status.CURRENT);
        turn.setCreateTime(LocalDateTime.now());
        conversationTurnMapper.insert(turn);
    }

    private List<Message> buildHistoryMessages(String conversationId) {
        List<AIConversationTurn> turns = conversationTurnMapper.selectByConversationId(conversationId);
        if (turns.isEmpty()) {
            return Collections.emptyList();
        }
        List<Message> messages = new ArrayList<>();
        List<AIConversationTurn> sortedTurns = turns.stream()
                .sorted(Comparator.comparing(AIConversationTurn::getRound)
                        .thenComparing(AIConversationTurn::getCreateTime))
                .toList();
        for (AIConversationTurn turn : sortedTurns) {
            if ("user".equals(turn.getRole())) {
                messages.add(new UserMessage("用户需求：" + turn.getContent()));
                continue;
            }
            if ("assistant".equals(turn.getRole())) {
                messages.add(new AssistantMessage(buildAssistantHistoryContent(turn.getContent(), turn.getRound())));
            }
        }
        return messages;
    }

    private String buildAssistantHistoryContent(String contentJson, Integer round) {
        List<SourceFile> files = deserializeCodeFiles(contentJson);
        List<Dependency> dependencies = deserializeDependencies(contentJson);
        String pluginName = deserializePluginName(contentJson);
        String pluginDescription = deserializePluginDescription(contentJson);
        StringBuilder sb = new StringBuilder();
        sb.append("第 ").append(round == null ? 0 : round).append(" 轮 AI 输出。\n");
        if (pluginName != null && !pluginName.isBlank()) {
            sb.append("插件名：").append(pluginName).append('\n');
        }
        if (pluginDescription != null && !pluginDescription.isBlank()) {
            sb.append("插件描述：").append(pluginDescription).append('\n');
        }
        if (!dependencies.isEmpty()) {
            sb.append("依赖：").append(dependencies).append('\n');
        }
        sb.append("代码快照：\n");
        for (SourceFile file : files) {
            sb.append("<file path=\"").append(file.getFilePath()).append("\">\n");
            sb.append(file.getContent()).append("\n</file>\n");
        }
        return sb.toString();
    }

    private void saveUserTurn(String conversationId, String pluginId, String userId, int round, String content) {
        AIConversationTurn userTurn = new AIConversationTurn();
        userTurn.setId(UUID.randomUUID().toString());
        userTurn.setConversationId(conversationId);
        userTurn.setPluginId(pluginId);
        userTurn.setUserId(userId);
        userTurn.setRound(round);
        userTurn.setRole("user");
        userTurn.setContent(content);
        userTurn.setStatus(AIConversationTurn.Status.DRAFT);
        userTurn.setCreateTime(LocalDateTime.now());
        conversationTurnMapper.insert(userTurn);
    }

    private String serializeCodeFiles(List<SourceFile> files,
                                        List<Dependency> dependencies,
                                        String pluginName,
                                        String pluginDescription,
                                        String rawResponse) {
        try {
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("files", files != null ? files : Collections.emptyList());
            wrapper.put("dependencies", dependencies != null ? dependencies : Collections.emptyList());
            wrapper.put("pluginName", pluginName);
            wrapper.put("pluginDescription", pluginDescription);
            wrapper.put("rawResponse", rawResponse);
            return objectMapper.writeValueAsString(wrapper);
        } catch (JsonProcessingException e) {
            log.error("序列化代码文件失败", e);
            return "{}";
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

    @SuppressWarnings("unchecked")
    private Object deserializeReviewResult(String contentJson) {
        try {
            if (contentJson != null && contentJson.trim().startsWith("{")) {
                Map<String, Object> map = objectMapper.readValue(contentJson, Map.class);
                return map.get("reviewResult");
            }
        } catch (Exception e) {
            log.warn("反序列化审查结果失败: {}", e.getMessage());
        }
        return null;
    }

    private String deserializePluginName(String contentJson) {
        return deserializeStringField(contentJson, "pluginName");
    }

    private String deserializePluginDescription(String contentJson) {
        return deserializeStringField(contentJson, "pluginDescription");
    }

    private String deserializeStringField(String contentJson, String fieldName) {
        try {
            if (contentJson != null && contentJson.trim().startsWith("{")) {
                Map<String, Object> map = objectMapper.readValue(contentJson, Map.class);
                Object value = map.get(fieldName);
                return value != null ? value.toString() : null;
            }
        } catch (Exception e) {
            log.warn("反序列化 {} 失败: {}", fieldName, e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String deserializeRawResponse(String contentJson) {
        try {
            if (contentJson != null && contentJson.trim().startsWith("{")) {
                Map<String, Object> map = objectMapper.readValue(contentJson, Map.class);
                Object raw = map.get("rawResponse");
                return raw != null ? raw.toString() : null;
            }
        } catch (Exception e) {
            log.warn("反序列化原始响应失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从 JSON 反序列化 Dependency 列表
     */
    @SuppressWarnings("unchecked")
    private List<Dependency> deserializeDependencies(String contentJson) {
        try {
            if (contentJson.trim().startsWith("{")) {
                Map<String, Object> map = objectMapper.readValue(contentJson, Map.class);
                Object depsObj = map.get("dependencies");
                if (depsObj instanceof List) {
                    return objectMapper.convertValue(depsObj, objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, Dependency.class));
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("反序列化依赖信息失败: {}", e.getMessage());
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
        tempVersion.setEntityPackage(normalizePackage(entityPackage, DEFAULT_ENTITY_PACKAGE));
        tempVersion.setMethodPackage(normalizePackage(methodPackage, DEFAULT_METHOD_PACKAGE));

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
        pluginVersion.setEntityPackage(normalizePackage(request.getEntityPackage(), DEFAULT_ENTITY_PACKAGE));
        pluginVersion.setMethodPackage(normalizePackage(request.getMethodPackage(), DEFAULT_METHOD_PACKAGE));

        pluginInfo.setPluginVersionList(Collections.singletonList(pluginVersion));

        ByteArrayMultipartFile multipartFile = new ByteArrayMultipartFile(
                "file", request.getName() + "-" + pluginVersion.getVersion() + ".jar", jarBytes);

        pluginService.add(pluginInfo, multipartFile);

        log.info("插件上传成功: name={}, pluginId={}, versionId={}",
                request.getName(), pluginId, versionId);
        return versionId;
    }

    private String normalizePackage(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
