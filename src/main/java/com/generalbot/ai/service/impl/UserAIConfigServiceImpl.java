package com.generalbot.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generalbot.ai.client.DynamicChatClientFactory;
import com.generalbot.ai.mapper.UserAIConfigMapper;
import com.generalbot.ai.entity.UserAIConfig;
import com.generalbot.ai.service.UserAIConfigService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class UserAIConfigServiceImpl extends ServiceImpl<UserAIConfigMapper, UserAIConfig> implements UserAIConfigService {

    private final DynamicChatClientFactory clientFactory;

    public UserAIConfigServiceImpl(DynamicChatClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /**
     * 测试用户AI配置
     * @param userId 用户ID
     */
    @Override
    public Boolean test(String userId) throws RuntimeException {
        ChatClient client = clientFactory.getPluginChatClient(userId);
        try {
            client.prompt()
                    .user("你好")
                    .call()
                    .content();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
