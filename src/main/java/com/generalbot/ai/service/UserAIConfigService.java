package com.generalbot.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.generalbot.ai.entity.UserAIConfig;

public interface UserAIConfigService extends IService<UserAIConfig> {

    /** 测试用户AI配置 */
    Boolean test(String userId) throws RuntimeException;

}
