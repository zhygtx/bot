package com.example.demo.core.engine.executor;

import com.example.demo.pojo.event.GroupMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.service.task.actionContent.ApiService;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.common.ActionRaw;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApiExecutor {
    // 注入 Bot 容器
    @Resource
    private BotContainer botContainer;

    private final ApiService apiService;

    @Autowired
    public ApiExecutor(ApiService apiService) {
        this.apiService = apiService;
    }

    public String executeApi(Action action, Object msg){
        log.debug("开始执行API动作，动作ID: {}, 数据ID: {}", action.getId(), action.getDataId());
        
        Api api = apiService.getApi(action.getDataId());
        log.debug("获取到API信息，API名称: {}", api.getName());
        
        String result = "";
        switch (api.getName()){
            case setGroupSpecialTitle:
                log.debug("执行设置群成员特殊头衔API");
                result = setGroupSpecialTitle((GroupMsg) msg, action.getExtractText().get(0));
                break;
            default:
                log.debug("未知API名称: {}", api.getName());
                break;
        }
        log.debug("API执行完成，返回结果: {}", result);
        return result;
    }

    /**
     * 设置群成员特殊头衔
     * @param groupMsg 群消息对象
     * @param specialTitle 特殊头衔
     * @return 设置结果
     */
    private String setGroupSpecialTitle(GroupMsg groupMsg, String specialTitle) {
        log.debug("开始设置群成员特殊头衔，群ID: {}, 用户ID: {}, 特殊头衔: {}", 
                groupMsg.getGroupId(), groupMsg.getUserId(), specialTitle);
        
        try {
            if (specialTitle.getBytes().length>18){
                log.debug("头衔长度超过18个字节，返回错误信息");
                return "头衔长度不能超过18个字节";
            }
            
            Bot bot = botContainer.robots.get(groupMsg.getBotId());
            log.debug("获取到Bot对象，Bot ID: {}", groupMsg.getBotId());
            
            ActionRaw actionRaw;
            if(specialTitle.isEmpty() || specialTitle.equals(" ") || specialTitle.equals("/n")){
                log.debug("头衔为空，执行取消头衔操作");
                actionRaw = bot.setGroupSpecialTitle(groupMsg.getGroupId(), groupMsg.getUserId(), "",-1);
                if (!actionRaw.getStatus().equals("ok")){
                    log.debug("取消头衔失败，状态: {}", actionRaw.getStatus());
                    return "取消头衔失败";
                }
                log.debug("取消头衔成功");
                return "取消头衔成功";
            }else {
                log.debug("执行设置特殊头衔操作");
                actionRaw = bot.setGroupSpecialTitle(groupMsg.getGroupId(), groupMsg.getUserId(), specialTitle,-1);
                if (!actionRaw.getStatus().equals("ok")){
                    log.debug("设置头衔失败，状态: {}", actionRaw.getStatus());
                    return "设置头衔失败" +specialTitle;
                }
                log.debug("设置头衔成功");
                return "设置头衔成功" +specialTitle;
            }
        } catch (Exception e) {
            log.debug("设置头衔发生异常: {}", e.getMessage(), e);
            return "设置头衔失败"+e.getMessage();
        }
    }
}