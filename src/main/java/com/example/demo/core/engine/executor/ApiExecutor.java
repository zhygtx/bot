package com.example.demo.core.engine.executor;

import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.service.task.actionContent.ApiService;
import com.example.demo.utils.HTMLUtil;
import com.gbx.warframe.worldstate.pojo.Fissure;
import com.gbx.warframe.worldstate.service.FissureService;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.common.ActionRaw;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ApiExecutor {
    // 注入 Bot 容器
    @Resource
    private BotContainer botContainer;

    private final FissureService fissureService;
    private final ApiService apiService;

    @Autowired
    public ApiExecutor(ApiService apiService, FissureService fissureService) {
        this.apiService = apiService;
        this.fissureService = fissureService;
    }

    public String executeApi(Action action, Object msg){
        Api api = apiService.getApi(action.getDataId());
        String result = "";
        switch (api.getName()){
            case setGroupSpecialTitle:
                result = setGroupSpecialTitle((GroupMsg) msg, action.getExtractText());
                break;
            case getWarframeFissure:
                result = getWarframeFissure(action.getExtractText());
            default:
                break;
        }
        return result;
    }

    /**
     * 设置群成员特殊头衔
     * @param groupMsg 群消息对象
     * @param specialTitle 特殊头衔
     * @return 设置结果
     */
    private String setGroupSpecialTitle(GroupMsg groupMsg, String specialTitle) {
        try {
            if (specialTitle.getBytes().length>18){
                return "头衔长度不能超过18个字节";
            }
            Bot bot = botContainer.robots.get(groupMsg.getBotId());
            ActionRaw actionRaw;
            if(specialTitle.isEmpty() || specialTitle.equals(" ") || specialTitle.equals("/n")){
                actionRaw = bot.setGroupSpecialTitle(groupMsg.getGroupId(), groupMsg.getUserId(), "",-1);
                if (!actionRaw.getStatus().equals("ok")){
                    return "取消头衔失败";
                }
                return "取消头衔成功";
            }else {
                actionRaw = bot.setGroupSpecialTitle(groupMsg.getGroupId(), groupMsg.getUserId(), specialTitle,-1);
                if (!actionRaw.getStatus().equals("ok")){
                    return "设置头衔失败" +specialTitle;
                }
                return "设置头衔成功" +specialTitle;
            }
        } catch (Exception e) {
            return "设置头衔失败"+e.getMessage();
        }
    }

    /**
     * 获取战区裂隙信息
     * @param key 搜索关键字
     * @return 裂隙信息
     */
    private String getWarframeFissure(String key){
        List<Fissure> fissures = fissureService.getFissures(key);
        if (fissures.isEmpty()){
            return "无相关裂隙";
        }
        //按裂隙等级排序
        Map<String, List<Fissure>> fissureMap = fissures.stream()
                .sorted(Comparator.comparingInt(Fissure::getModifierLevel))
                .collect(Collectors.groupingBy(
                        Fissure::getModifier,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return MsgUtils.builder()
                .img("base64://"+HTMLUtil.toImage(fissureMap))
                .build();
    }
}