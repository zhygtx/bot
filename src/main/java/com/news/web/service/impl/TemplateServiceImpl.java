package com.news.web.service.impl;

import com.news.bot.wf.pojo.Fissure;
import com.news.bot.wf.service.FissureService;
import com.news.bot.wf.utils.FissuresUtil;
import com.news.bot.wf.utils.HtmlToImageUtil;
import com.news.web.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板服务实现类
 * 提供模板渲染为图片的功能，并支持缓存机制
 */
@Slf4j
@Service
public class TemplateServiceImpl implements TemplateService {

    /**
     * HTML转图片工具类实例
     */
    @Autowired
    private HtmlToImageUtil htmlToImageUtil;

    /**
     * Redis操作模板实例
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 裂隙服务实例
     */
    @Autowired
    private FissureService fissureService;


    /**
     * 预定义的模板名称列表
     */
    private static final Map<String, String> TEMPLATE_NAMES = new HashMap<>() {{
        put("fissure", "裂隙模板");
        put("fissure_test", "测试裂隙模板");
    }};

    /**
     * 获取所有模板渲染后的图片
     *
     * @return 包含模板名称和对应Base64图片字符串的映射关系
     */
    @Override
    public Map<String, String> getAllTemplateImages() {
        Map<String, String> templateImages = new HashMap<>();
        int width = 750;

        // 遍历所有预定义模板名称
        for (Map.Entry<String, String> entry : TEMPLATE_NAMES.entrySet()) {
            String templateName = entry.getKey();    // 文件名
            String displayName = entry.getValue();   // 展示名

            try {
                String cacheKey = "template:image:" + templateName + ":" + width;

                String cachedImage = stringRedisTemplate.opsForValue().get(cacheKey);
                if (cachedImage != null) {
                    templateImages.put(displayName, cachedImage);
                    continue;
                }

                // 获取所有裂隙数据并按层级分组
                List<Fissure> allFissures = fissureService.selectByKey("裂隙");
                Map<String, List<Fissure>> groupedFissures = FissuresUtil.groupFissuresByTier(allFissures);

                Map<String, Object> data = new HashMap<>();
                data.put("data", groupedFissures);

                String imageBase64 = htmlToImageUtil.renderToBase64(templateName, data, width);
                stringRedisTemplate.opsForValue().set(cacheKey, imageBase64, Duration.ofMinutes(30));

                templateImages.put(displayName, imageBase64);
            } catch (Exception e) {
                log.error("渲染模板 {} 失败", templateName, e);
            }
        }

        return templateImages;
    }

    /**
     * 预渲染所有模板
     * 直接调用getAllTemplateImages方法触发模板渲染及缓存
     */
    @Override
    public void preRenderAllTemplates() {
        getAllTemplateImages();
    }
}