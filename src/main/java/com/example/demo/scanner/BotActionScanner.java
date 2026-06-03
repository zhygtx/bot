package com.example.demo.scanner;

import com.example.demo.annotation.ActionParam;
import com.example.demo.annotation.BotAction;
import com.example.demo.api.BotActionService;
import com.example.demo.metadata.ActionMetadata;
import com.example.demo.pojo.entity.plugin.ParameterInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * BOT 动作元数据扫描器
 * 自动扫描并注册所有带有注解的 BOT 动作
 */
@Component
@Slf4j
public class BotActionScanner {

    private final List<ActionMetadata> actionMetadataList = new ArrayList<>();

    @PostConstruct
    public void init() {
        scanActions();
        log.info("BOT动作扫描完成: {}个动作",
                actionMetadataList.size());
    }

    /**
     * 扫描所有动作方法
     */
    private void scanActions() {
        try {
            Class<BotActionService> serviceClass = BotActionService.class;

            for (Method method : serviceClass.getDeclaredMethods()) {
                BotAction botAction = method.getAnnotation(BotAction.class);
                if (botAction != null) {
                    ActionMetadata metadata = extractActionMetadata(method, botAction);
                    actionMetadataList.add(metadata);
                    log.info("扫描到BOT动作: {} ({})", botAction.name(), method.getName());
                }
            }

            actionMetadataList.sort(Comparator.comparingInt(ActionMetadata::getOrder));
        } catch (Exception e) {
            log.error("扫描BOT动作失败", e);
        }
    }

    /**
     * 提取动作元数据
     */
    private ActionMetadata extractActionMetadata(Method method, BotAction botAction) {
        List<ParameterInfo> parameters = new ArrayList<>();
        Parameter[] methodParams = method.getParameters();

        for (int i = 0; i < methodParams.length; i++) {
            Parameter param = methodParams[i];
            ActionParam actionParam = param.getAnnotation(ActionParam.class);

            String description = actionParam != null ? actionParam.description() : "参数";
            int order = actionParam != null ? actionParam.order() : i;

            ParameterInfo paramInfo = ParameterInfo.builder()
                    .id(String.valueOf(i + 1))
                    .name(param.getName())
                    .type(getSimpleTypeName(param.getType()))
                    .description(description)
                    .order(order)
                    .build();
            parameters.add(paramInfo);
        }

        return ActionMetadata.builder()
                .actionName(method.getName())
                .actionDisplayName(botAction.name())
                .description(botAction.description())
                .order(botAction.order())
                .parameters(parameters)
                .build();
    }

    /**
     * 获取类型的简化名称
     */
    private String getSimpleTypeName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getSimpleTypeName(clazz.getComponentType()) + "[]";
        }

        String name = clazz.getSimpleName();

        return switch (name) {
            case "boolean" -> "boolean";
            case "byte" -> "byte";
            case "short" -> "short";
            case "int" -> "int";
            case "long" -> "Long";
            case "float" -> "float";
            case "double" -> "double";
            case "char" -> "char";
            default -> name;
        };

    }

    /**
     * 获取所有动作元数据
     */
    public List<ActionMetadata> getAllActions() {
        return Collections.unmodifiableList(actionMetadataList);
    }

    /**
     * 获取BOT动作方法的参数数量
     * @param methodName 方法名
     * @return 参数数量
     */
    public int getMethodParamCount(String methodName) {
        for (ActionMetadata metadata : actionMetadataList) {
            if (metadata.getActionName().equals(methodName)) {
                return metadata.getParameters().size();
            }
        }
        return 0;
    }

    /**
     * 获取BOT动作方法的参数索引
     * @param methodName 方法名
     * @param paramName 参数名
     * @return 参数索引
     */
    public int getMethodParamIndex(String methodName, String paramName) {
        for (ActionMetadata metadata : actionMetadataList) {
            if (metadata.getActionName().equals(methodName)) {
                List<ParameterInfo> params = metadata.getParameters();
                for (int i = 0; i < params.size(); i++) {
                    if (params.get(i).getName().equals(paramName)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
