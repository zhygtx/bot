package com.example.demo.core.engine.executor;

import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.service.task.actionContent.ApiService;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.common.ActionRaw;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApiExecutor {
    // 注入 Bot 容器
    @Resource
    private BotContainer botContainer;

    private final ApiService apiService;

    @Autowired
    public ApiExecutor(ApiService apiService) {
        this.apiService = apiService;
    }

    public String executeApi(Action action, GroupMsg groupMsg){
        Api api = apiService.getApi(action.getDataId());
        String result = "";
        switch (api.getName()){
            case setGroupSpecialTitle:
                result = setGroupSpecialTitle(groupMsg, action.getExtractText());
                break;
            default:
                break;
        }
        return result;
    }

    private String setGroupSpecialTitle(GroupMsg groupMsg, String specialTitle) {
        try {
            if (specialTitle.getBytes().length>18){
                return "头衔长度不能超过18个字节";
            }
            Bot bot = botContainer.robots.get(groupMsg.getBotId());
            ActionRaw actionRaw = bot.setGroupSpecialTitle(groupMsg.getGroupId(), groupMsg.getUserId(), specialTitle,-1);
            if (!actionRaw.getStatus().equals("ok")){
                return "设置头衔失败";
            }
            return "设置头衔成功："+specialTitle;
        } catch (Exception e) {
            return "设置头衔失败"+e.getMessage();
        }
    }
}
