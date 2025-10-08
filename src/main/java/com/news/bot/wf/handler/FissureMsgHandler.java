package com.news.bot.wf.handler;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.news.bot.service.GroupPermissionService;
import com.news.bot.wf.service.FissureService;
import com.news.bot.wf.pojo.Fissure;
import com.news.bot.wf.pojo.FissureTask;
import com.news.bot.wf.task.FissureUpdateTask;
import com.news.bot.utils.BotUtil;
import com.news.bot.wf.utils.FissureTaskUtil;
import com.news.bot.wf.utils.FissuresUtil;
import com.news.bot.wf.utils.HtmlToImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;

@Component
@Shiro
public class FissureMsgHandler {

    @Autowired
    private FissureService fissureService;

    @Autowired
    private HtmlToImageUtil htmlToImageUtil;

    @Autowired
    private FissureUpdateTask fissureUpdateTask;

    @Autowired
    private GroupPermissionService groupPermissionService;

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = "更新")
    public void upDate(Bot bot, GroupMessageEvent event, Matcher matcher) {
        fissureUpdateTask.updateFissureData();
    }

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = "裂隙|裂缝|钢铁裂隙|钢铁裂缝|九重天|生存|防御|歼灭|中断|挖掘|救援|前哨战")
    public void fissures(Bot bot, GroupMessageEvent event, Matcher matcher) {
        if(groupPermissionService.notHasPermission(event.getGroupId(), "wf")){
            return;
        }

        String msg = matcher.group();

        List<Fissure> fissureList = fissureService.selectByKey(msg);

        if (fissureList == null || fissureList.isEmpty()) {
            bot.sendGroupMsg(event.getGroupId(), "❌ 无符合的裂隙", false);
            return;
        }

        // 构建模板所需的数据模型
        Map<String, Object> data = new HashMap<>();
        data.put("data", FissuresUtil.groupFissuresByTier(fissureList));

        String templateName = fissureService.selectTemplateName(event.getGroupId(), event.getUserId());
        // 如果用户没有选择模板，则使用默认模板
        if (templateName == null || templateName.isEmpty()) {
            templateName = "fissure"; // 默认模板名称
        }

        // 异步生成 Base64 图片
        htmlToImageUtil.renderToBase64Async(templateName, data,750).thenAccept(base64 -> {
            base64 = base64.replaceAll("\\s+", "");
            String cqCode = "[CQ:image,file=" + base64 + "]";
            bot.sendGroupMsg(event.getGroupId(), cqCode, false );
        });
    }

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = "查询蹲")
    public void selectFissureTask(Bot bot, GroupMessageEvent event, Matcher matcher) {
        if(groupPermissionService.notHasPermission(event.getGroupId(), "wf")){
            return;
        }

        List<FissureTask> tasks = fissureService.selectFissureTask(event);
        if (tasks.isEmpty()) {
            bot.sendGroupMsg(event.getGroupId(), "❌ 未找到您所添加的任务或常驻任务", false);
            return;
        }
        StringBuilder message = new StringBuilder("您所添加的与常驻任务：\n");
        for (FissureTask task : tasks) {
            message.append("任务ID：").append(task.getId()).append("\n")
                    .append("地图：").append(task.getNode()).append("\n")
                    .append("任务类型：").append(task.getMissionType()).append("\n")
                    .append("是否钢铁：").append(task.getIsHard()).append("\n")
                    .append("是否常驻：").append(task.getIsPersist()).append("\n").append("\n");
        }
        message.append("共").append(tasks.size()).append("条任务");
        bot.sendGroupMsg(event.getGroupId(), message.toString(), false);
    }

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = "^蹲\\s.*$|^常驻蹲\\s.*$|^取消蹲\\s.*$|^取消常驻蹲\\s.*$|取消蹲|取消常驻蹲")
    public void setFissureTask(Bot bot, GroupMessageEvent event, Matcher matcher) {
        if(groupPermissionService.notHasPermission(event.getGroupId(), "wf")){
            return;
        }

        String rawCommand = matcher.group();
        String[] parts = rawCommand.split("\\s+");
        String commandType = parts[0];

        long groupId = event.getGroupId();
        String userRole = BotUtil.getUserRole(bot, event);

        // 使用工具类方法
        FissureTaskUtil.CommandContext ctx = FissureTaskUtil.parseCommandType(commandType, userRole, groupId, bot);
        if (ctx == null) return;

        FissureTaskUtil.ParsedParams params = FissureTaskUtil.extractParameters(parts);
        if (!FissureTaskUtil.validateFissureInput(params, commandType, event, bot)) return;

        FissureTaskUtil.executeFissureTask(bot, event, ctx, params, fissureService);
    }

}