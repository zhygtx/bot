package com.generalbot.bot.metadata;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 动作返回值元数据。
 * 描述 BotAction 方法的返回值结构，供前端做数据映射。
 */
@Data
@Builder
public class ActionReturnInfo {

    /** 返回值业务类型简名（去掉 CompletableFuture/ApiResponse 包装后的实际类型） */
    private String type;

    /** 返回值描述 */
    private String description;

    /** 平铺后的返回字段列表（当返回值为复杂对象时；void/基本类型为空或含单个 value 字段） */
    private List<ReturnFieldInfo> fields;
}
