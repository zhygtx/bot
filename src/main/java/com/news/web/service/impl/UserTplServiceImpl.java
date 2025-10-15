package com.news.web.service.impl;

import com.news.web.mapper.UserTplMapper;
import com.news.web.pojo.UserTpl;
import com.news.web.service.UserTplService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import static com.news.web.utils.TemplateUtil.TEMPLATE_NAMES;

@Service
public class UserTplServiceImpl implements UserTplService {

    @Autowired
    private UserTplMapper userTplMapper;

    @Override
    public int insert(UserTpl userTpl) {
        String displayName = userTpl.getTemplateName();
        String templateName = TEMPLATE_NAMES.entrySet().stream()
                .filter(entry -> entry.getValue().equals(displayName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        userTpl.setTemplateName(templateName);
        return userTplMapper.insert(userTpl);
    }

    @Override
    public Boolean hasUserTpl(String id){
        return userTplMapper.hasUserTpl(id);
    }

    @Override
    public UserTpl selectByUserId(Long userId){
        return userTplMapper.selectByUserId(userId);
    }

    @Override
    public void update(UserTpl userTpl){
        userTplMapper.update(userTpl);
    }

    @Override
    public void delete(UserTpl userTpl){
        userTplMapper.delete(userTpl);
    }

}
