package com.example.demo.controller;

import com.example.demo.metadata.ActionMetadata;
import com.example.demo.metadata.EventMetadata;
import com.example.demo.pojo.Result;
import com.example.demo.util.BotMetadataScanner;
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

    private final BotMetadataScanner metadataScanner;

    public BotInterfaceController(BotMetadataScanner metadataScanner) {
        this.metadataScanner = metadataScanner;
    }

    /**
     * 获取 BOT 事件列表
     * @return BOT 事件列表
     */
    @GetMapping("/events")
    public Result<?> getBotEvents() {
        List<EventMetadata> events = metadataScanner.getAllEvents();
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