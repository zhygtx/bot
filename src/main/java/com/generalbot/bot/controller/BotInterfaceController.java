package com.generalbot.bot.controller;

import com.generalbot.bot.metadata.ActionMetadata;
import com.generalbot.bot.metadata.EventMetadata;
import com.generalbot.common.api.Result;
import com.generalbot.bot.scanner.BotEventScanner;
import com.generalbot.bot.scanner.BotActionScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BOT 接口控制器
 * 提供前端获取 BOT 事件与 BOT 动作相关方法内容的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/bot")
public class BotInterfaceController {

    private final BotActionScanner metadataScanner;
    private final BotEventScanner botEventScanner;

    public BotInterfaceController(BotActionScanner metadataScanner, BotEventScanner botEventScanner) {
        this.metadataScanner = metadataScanner;
        this.botEventScanner = botEventScanner;
    }

    /**
     * 获取 BOT 事件列表
     * @return BOT 事件列表
     */
    @GetMapping("/events")
    public Result<?> getBotEvents() {
        List<EventMetadata> events = botEventScanner.getEventMetadataList();
        return Result.success(null, events);
    }

    /**
     * 获取 BOT 动作列表
     * @return BOT 动作列表
     */
    @GetMapping("/actions")
    public Result<?> getBotActions() {
        List<ActionMetadata> actions = metadataScanner.getAllActions();
        return Result.success(null, actions);
    }
}
