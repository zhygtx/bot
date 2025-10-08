// com.news.utils.FissureTaskUtil.java
package com.news.bot.wf.utils;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.news.bot.wf.pojo.Fissure;
import com.news.bot.wf.pojo.FissureTask;
import com.news.bot.wf.service.FissureService;

import java.util.*;

// ... existing code ...

public class FissureTaskUtil {
    // 示例地图列表
    public static final Set<String> MAP_KEYWORDS = new HashSet<>(Set.of(
            "金星", "地球", "月球", "火星", "火卫一", "火卫二", "谷神星", "木星", "欧罗巴",
            "土星", "天王星", "海王星", "冥王星", "赛德娜", "赤毒要塞", "阋神星", "虚空", "扎里曼"
    ));

    // 示例任务类型列表
    public static final Set<String> MISSION_KEYWORDS = new HashSet<>(Set.of(
            "歼灭", "防御", "救援", "挖掘", "中断", "生存", "拦截", "虚空覆涌", "虚空洪流", "元素转换",
            "破坏", "前哨战", "移动防御","捕获"
    ));

    public static final Set<String> TIER_KEYWORDS = new HashSet<>(Set.of(
            "古纪", "前纪", "中纪", "后纪", "安魂", "全能"
    ));

    public static boolean isStormKeyword(String param) {
        return "九重天".equals(param);
    }

    public static boolean isTier(String keyword) {
        return TIER_KEYWORDS.contains(keyword);
    }

    public static boolean isMap(String keyword) {
        return MAP_KEYWORDS.contains(keyword);
    }

    public static boolean isMission(String keyword) {
        return MISSION_KEYWORDS.contains(keyword);
    }

    public static boolean isHardKeyword(String param) {
        return "钢铁".equals(param) || "hard".equalsIgnoreCase(param) || "steeled".equalsIgnoreCase(param);
    }

    public static boolean isNormalKeyword(String param) {
        return "普通".equals(param) || "normal".equalsIgnoreCase(param);
    }

    // 地图匹配逻辑
    public static boolean isNodeMatch(String fissureNode, String taskNode) {
        return taskNode == null ||
                fissureNode.contains(taskNode) ||
                taskNode.contains(fissureNode);
    }

    // 任务类型必须完全相等
    public static boolean isMissionMatch(String fissureMissionType, String taskMissionType) {
        return taskMissionType == null || Objects.equals(fissureMissionType, taskMissionType);
    }

    // 等级必须完全相等或为空
    public static boolean isTierMatch(String fissureTier, String taskTier) {
        return taskTier == null  || fissureTier.equals(taskTier);
    }

    // 钢铁裂隙必须完全相等或为空
    public static boolean isHardMatch(Boolean fissureIsHard, Boolean taskIsHard) {
        return taskIsHard == null  || fissureIsHard.equals(taskIsHard);
    }

    public static boolean isStormMatch(Boolean fissureIsStorm, Boolean taskIsStorm) {
        return fissureIsStorm.equals(taskIsStorm);
    }

    // 综合匹配规则
    public static boolean isValidMatch(Fissure fissure, FissureTask task) {

        boolean nodeMatch = isNodeMatch(fissure.getNode(), task.getNode());
        boolean missionMatch = isMissionMatch(fissure.getMissionType(), task.getMissionType());
        boolean tierMatch = isTierMatch(fissure.getTier(), task.getTier());
        boolean hardMatch = isHardMatch(fissure.getIsHard(), task.getIsHard());
        boolean stormMatch = isStormMatch(fissure.getIsStorm(), task.getIsStorm());

        return nodeMatch && missionMatch && tierMatch && hardMatch&& stormMatch;
    }


    public static void sendFissureMessage(Bot bot,
                                          Map<Long, Map<Fissure, Set<Long>>> allPushData) {

        allPushData.forEach((groupId, fissureMap) -> fissureMap.forEach((fissure, userSet) -> {
            String messageType = Boolean.TRUE.equals(fissure.getIsHard())
                    ? "钢铁" : "普通";
            String body = String.format(
                    "[裂隙推送] %s %s %s %s\n剩余时间：%s\n",
                    messageType,
                    fissure.getNode(),
                    fissure.getTier(),
                    fissure.getMissionType(),
                    fissure.getEta()
            );
            MsgUtils builder = MsgUtils.builder().text(body);
            userSet.forEach(builder::at);
            String message = builder.build();
            bot.sendGroupMsg(groupId, message, false);
        }));
    }

    public static CommandContext parseCommandType(String commandType, String userRole, long groupId, Bot bot) {
        boolean isPersist = false;
        boolean isDelete = false;

        if (commandType.startsWith("常驻蹲")) {
            if ("member".equals(userRole)) {
                bot.sendGroupMsg(groupId, "❌ 只有群主或管理员可以设置常驻蹲", false);
                return null;
            }
            isPersist = true;
        } else if (commandType.startsWith("取消常驻蹲")) {
            isPersist = true;
            isDelete = true;
        } else if (commandType.startsWith("取消蹲")) {
            isDelete = true;
        }

        return new CommandContext(isPersist, isDelete);
    }

    public record CommandContext(boolean isPersist, boolean isDelete) {}


    public static ParsedParams extractParameters(String[] parts) {
        String map = null, mission = null, tier = null;
        Boolean isHard = null; boolean isStorm = false;
        List<String> invalidParams = new ArrayList<>();

        for (String param : Arrays.asList(parts).subList(1, parts.length)) {
            if (param == null || param.isEmpty()) continue;

            if (isMap(param)) {
                map = param;
            } else if (isMission(param)) {
                mission = param;
            } else if (isTier(param)) {
                tier = param;
            } else if (isHardKeyword(param)) {
                isHard = true;
            } else if (isNormalKeyword(param)) {
                isHard = false;
            } else if (isStormKeyword(param)) {  // 新增判断
                isStorm = true;
            } else {
                invalidParams.add(param);
            }
        }

        return new ParsedParams(map, mission, tier, isHard, isStorm,invalidParams);
    }

    public record ParsedParams(
            String map,
            String mission,
            String tier,
            Boolean isHard,
            Boolean isStorm,
            List<String> invalidParams
    ) {}

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateFissureInput(ParsedParams params, String commandType, GroupMessageEvent event, Bot bot) {
        boolean hasHard = Arrays.stream(commandType.split("\\s+")).anyMatch(FissureTaskUtil::isHardKeyword);
        boolean hasNormal = Arrays.stream(commandType.split("\\s+")).anyMatch(FissureTaskUtil::isNormalKeyword);

        if (hasHard && hasNormal) {
            sendMsg(bot, event, "❌ 不能同时指定「钢铁」和「普通」");
            return false;
        }

        if (hasNormal && params.isHard() == null) {
            params = new ParsedParams(
                    params.map(),
                    params.mission(),
                    params.tier(),
                    false,
                    false,
                    params.invalidParams()
            );
        }

        if ((commandType.startsWith("蹲") || commandType.startsWith("常驻蹲")) &&
                params.map == null && params.mission == null) {
            sendMsg(bot, event, "请指定地图和任务");
            return false;
        }

        if (!params.invalidParams.isEmpty()) {
            sendMsg(bot, event, "请指定地图和任务");
            return false;
        }

        return true;
    }

    public static void executeFissureTask(Bot bot, GroupMessageEvent event,
                                          CommandContext ctx, ParsedParams params,
                                          FissureService fissureService) {
        String id = event.getGroupId() + "-" + event.getUserId() + "-" + System.currentTimeMillis();

        FissureTask task = new FissureTask();
        task.setId(id);
        task.setGroupId(event.getGroupId());
        task.setUserId(event.getUserId());
        task.setNode(params.map);
        task.setMissionType(params.mission);
        task.setTier(params.tier);
        task.setIsHard(params.isHard);
        task.setIsPersist(ctx.isPersist);
        task.setIsStorm(params.isStorm);

        if (ctx.isDelete) {
            FissureTask taskDelete = new FissureTask();
            taskDelete.setGroupId(event.getGroupId());
            taskDelete.setUserId(event.getUserId());
            taskDelete.setNode(params.map);
            taskDelete.setMissionType(params.mission);
            taskDelete.setTier(params.tier);
            taskDelete.setIsPersist(ctx.isPersist);
            taskDelete.setIsHard(params.isHard);
            taskDelete.setIsStorm(params.isStorm);

            int deletedCount = fissureService.deleteFissureTask(taskDelete);

            if (deletedCount > 0) {
                sendMsg(bot,event, "✅ 已取消：" +
                        (params.map != null ? params.map : "") + " " +
                        (params.tier != null ? params.tier : "") + " " +
                        (params.mission != null ? params.mission : "") +
                        (params.isHard != null && params.isHard ? "（钢铁）" : ""));
            } else {
                sendMsg(bot,event, "❌ 未找到您所添加的任务");
            }
        } else {
            fissureService.insertFissureTask(task);
            sendMsg(bot,event, "✅ 已设置：" +
                    (params.map != null ? params.map : "") + " " +
                    (params.tier != null ? params.tier : "") + " " +
                    (params.mission != null ? params.mission : "") +
                    (params.isHard != null && params.isHard ? "（钢铁）" : ""));
        }
    }


    private static void sendMsg(Bot bot,GroupMessageEvent event, String msg){
        if (event.getGroupId().equals(event.getUserId())){
            bot.sendPrivateMsg(event.getUserId(), msg, false);
        } else{
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        }
    }
}
