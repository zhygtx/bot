package com.generalbot.bot.scanner;

import com.generalbot.bot.metadata.ActionMetadata;
import com.generalbot.bot.metadata.ActionReturnInfo;
import com.generalbot.bot.metadata.ReturnFieldInfo;
import com.generalbot.plugin.entity.ParameterInfo;
import com.github.zhygtx.napcat.protocol.ActionDescriptor;
import com.github.zhygtx.napcat.protocol.ActionParamDescriptor;
import com.github.zhygtx.napcat.protocol.ActionResponseDescriptor;
import com.github.zhygtx.napcat.protocol.ProtocolCatalog;
import com.github.zhygtx.napcat.protocol.ProtocolField;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * BOT 动作元数据适配器。
 * <p>
 * 数据来源为 SDK 的 {@link ProtocolCatalog}，不再反射扫描 typed 动作接口。
 */
@Component
@Slf4j
public class BotActionScanner {

    private final List<ActionMetadata> actionMetadataList = new ArrayList<>();

    private final ProtocolCatalog protocolCatalog;

    public BotActionScanner(ProtocolCatalog protocolCatalog) {
        this.protocolCatalog = protocolCatalog;
    }

    @PostConstruct
    public void init() {
        for (ActionDescriptor action : protocolCatalog.getActions()) {
            actionMetadataList.add(toMetadata(action));
        }
        actionMetadataList.sort(Comparator.comparingInt(ActionMetadata::getOrder));
        log.info("BOT动作目录加载完成，共 {} 个动作", actionMetadataList.size());
    }

    private ActionMetadata toMetadata(ActionDescriptor action) {
        List<ParameterInfo> parameters = new ArrayList<>();
        List<ActionParamDescriptor> params = action.getParams() == null
                ? List.of()
                : action.getParams();
        for (int i = 0; i < params.size(); i++) {
            ActionParamDescriptor param = params.get(i);
            parameters.add(ParameterInfo.builder()
                    .id(String.valueOf(i + 1))
                    .name(param.getName())
                    .type(simpleType(param.getType()))
                    .description(param.getDescription())
                    .order(i)
                    .nullable(param.isNullable())
                    .build());
        }

        ActionResponseDescriptor response = action.getResponse();
        String returnType = response == null ? "void" : response.getType();
        String returnDescription = response == null ? "" : response.getDescription();
        List<ReturnFieldInfo> returnFields = new ArrayList<>();
        if (response != null && response.getFields() != null) {
            for (int i = 0; i < response.getFields().size(); i++) {
                ProtocolField field = response.getFields().get(i);
                returnFields.add(ReturnFieldInfo.builder()
                        .name(field.getName())
                        .type(field.getType())
                        .description(field.getDescription())
                        .fieldPath(field.getPath())
                        .inherited(false)
                        .order(i)
                        .build());
            }
        }

        List<String> categories = action.getTags() == null || action.getTags().isEmpty()
                ? Collections.singletonList("其他")
                : action.getTags();
        return ActionMetadata.builder()
                .actionName(action.getName())
                .actionDisplayName(action.getSummary() == null || action.getSummary().isBlank()
                        ? action.getName() : action.getSummary())
                .description(action.getDescription() == null || action.getDescription().isBlank()
                        ? action.getSummary() : action.getDescription())
                .order(0)
                .parameters(parameters)
                .categories(categories)
                .categoryOrders(IntStream.range(0, categories.size()).boxed().toList())
                .returnInfo(ActionReturnInfo.builder()
                        .type(returnType)
                        .description(returnDescription)
                        .fields(returnFields)
                        .build())
                .build();
    }

    private String simpleType(String type) {
        if (type == null) {
            return "Object";
        }
        if (type.startsWith("List<") || type.startsWith("Set<") || type.startsWith("Collection<")) {
            return "List";
        }
        if (type.startsWith("Map<")) {
            return "Map";
        }
        return type;
    }

    public List<ActionMetadata> getAllActions() {
        return Collections.unmodifiableList(actionMetadataList);
    }

    public int getMethodParamCount(String methodName) {
        for (ActionMetadata metadata : actionMetadataList) {
            if (metadata.getActionName().equals(methodName)) {
                return metadata.getParameters().size();
            }
        }
        return 0;
    }

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
