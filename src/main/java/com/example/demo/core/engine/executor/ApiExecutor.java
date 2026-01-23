package com.example.demo.core.engine.executor;

import com.example.demo.pojo.event.GroupMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.service.task.actionContent.ApiService;
import com.example.demo.utils.StringTemplateUtil;
import com.gbx.warframe.worldstate.pojo.Fissure;
import com.gbx.warframe.worldstate.service.FissureService;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.common.ActionRaw;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ApiExecutor {
    // 注入 Bot 容器
    @Resource
    private BotContainer botContainer;

    private final ApiService apiService;
    private final StringTemplateUtil stringTemplateUtil;
    private final FissureService fissureService;

    public ApiExecutor(ApiService apiService, StringTemplateUtil stringTemplateUtil, FissureService fissureService) {
        this.apiService = apiService;
        this.stringTemplateUtil = stringTemplateUtil;
        this.fissureService = fissureService;
    }

    public String executeApi(Action action, Object msg){
        log.debug("开始执行API动作，动作ID: {}, 数据ID: {}", action.getId(), action.getDataId());
        
        Api api = apiService.getApiById(action.getDataId());
        log.debug("获取到API信息，API名称: {}", api.getName());

        Map<String, String> params = api.getParams();
        for (Map.Entry<String, String> entry : api.getParams().entrySet()){
            params.put(entry.getKey(), stringTemplateUtil.render(entry.getValue(), msg, action.getExtractText()));
        }
        log.debug("参数渲染完成，参数列表: {}", params);

        String result = "";
        switch (api.getName()){
            case setGroupSpecialTitle -> result = setGroupSpecialTitle((GroupMsg) msg, params);
            case getWarframeFissure -> result = getWarframeFissure(params).toString();
            default -> log.debug("未知API名称: {}", api.getName());
        }
        log.debug("API执行完成，返回结果: {}", result);
        return result;
    }

    /**
     * 设置群成员特殊头衔
     * @param groupMsg 群消息对象
     * @param params 参数列表
     * @return 设置结果
     */
    private String setGroupSpecialTitle(GroupMsg groupMsg, Map<String, String> params) {
        log.debug("开始设置群成员特殊头衔，群ID: {}, 用户ID: {}, 特殊头衔: {}", 
                groupMsg.getGroupId(), groupMsg.getUserId(), params.get("specialTitle"));
        
        try {
            if (params.get("specialTitle").getBytes().length>18){
                log.debug("头衔长度超过18个字节，返回错误信息");
                return "头衔长度不能超过18个字节";
            }
            
            Bot bot = botContainer.robots.get(groupMsg.getBotId());
            log.debug("获取到Bot对象，Bot ID: {}", groupMsg.getBotId());
            
            ActionRaw actionRaw;
            log.debug("执行设置特殊头衔操作");
            actionRaw = bot.setGroupSpecialTitle(groupMsg.getGroupId(), groupMsg.getUserId(), params.get("specialTitle"), Integer.parseInt(params.get("duration")));
            if (!actionRaw.getStatus().equals("ok")){
                log.debug("设置头衔失败，状态: {}", actionRaw.getStatus());
                return "设置头衔失败" + params.get("specialTitle");
            }
            log.debug("设置头衔成功");
            return "设置成功" + params.get("specialTitle");
        } catch (Exception e) {
            log.debug("设置头衔发生异常: {}", e.getMessage(), e);
            return "设置头衔失败"+e.getMessage();
        }
    }

    /**
     * 获取战区裂隙信息
     * @param params 参数列表，包含 搜索关键字
     * @return 裂隙信息
     */
    public Object getWarframeFissure(Map<String, String> params) {
        String key = params.get("key");
        log.debug("开始获取战区裂隙信息，关键字: {}", key);

        List<Fissure> fissures = fissureService.getFissures(key);
        log.debug("获取到裂隙数量: {}", fissures.size());

        if (fissures.isEmpty()){
            log.debug("无相关裂隙，返回提示信息");
            return "无相关裂隙";
        }

        //按裂隙等级排序
        log.debug("开始按裂隙等级排序");
        Map<String, List<Fissure>> fissureMap = fissures.stream()
                .sorted(Comparator.comparingInt(Fissure::getModifierLevel))
                .collect(Collectors.groupingBy(
                        Fissure::getModifier,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        log.debug("排序完成，裂隙分组数量: {}", fissureMap.size());
        return fissureMap;
    }
}