package com.news.bot.wf.handler;

import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.news.bot.wf.pojo.FissureTask;
import com.news.bot.wf.service.FissureService;
import com.news.bot.wf.utils.FissureTaskUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;

@Component
@Shiro
public class PrivateFissureMsgHandler {

    @Autowired
    private FissureService fissureService;

    @PrivateMessageHandler
    @MessageHandlerFilter(cmd = "查询蹲")
    public void selectFissureTask(Bot bot, PrivateMessageEvent event, Matcher matcher) {

        // 将私聊消息伪装为群聊消息便于统一处理
        GroupMessageEvent gevent = new GroupMessageEvent();
        gevent.setGroupId(event.getUserId());


        List<FissureTask> tasks = fissureService.selectFissureTask(gevent);
        if (tasks.isEmpty()) {
            bot.sendPrivateMsg(gevent.getGroupId(), "❌ 未找到您所添加的订阅", false);
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
        bot.sendPrivateMsg(gevent.getGroupId(), message.toString(), false);
    }

    @PrivateMessageHandler
    @MessageHandlerFilter(cmd = "^蹲\\s.*$|^常驻蹲\\s.*$|^取消蹲\\s.*$|^取消常驻蹲\\s.*$|取消蹲|取消常驻蹲")
    public void setFissureTask(Bot bot, PrivateMessageEvent event, Matcher matcher) {

        //结构化消息
        String rawCommand = matcher.group();
        String[] parts = rawCommand.split("\\s+");
        String commandType = parts[0];

        //将私聊消息伪装为群消息(便于处理)
        long groupId = event.getUserId();
        long userId = event.getUserId();
        GroupMessageEvent gevent = new GroupMessageEvent();
        gevent.setGroupId(groupId);
        gevent.setUserId(userId);

        //判断是否为取消蹲
        boolean isDelete = commandType.startsWith("取消蹲");
        //结构化首指令，便于判断
        FissureTaskUtil.CommandContext ctx = new FissureTaskUtil.CommandContext(true,isDelete);

        //结构化具体指定的内容
        FissureTaskUtil.ParsedParams params = FissureTaskUtil.extractParameters(parts);

        //验证输入指令是否合理
        if (!FissureTaskUtil.validateFissureInput(params, commandType, gevent, bot)) return;

        FissureTaskUtil.executeFissureTask(bot, gevent, ctx, params, fissureService);
    }

}
