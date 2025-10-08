package com.news.web.service;

import java.util.Map;

public interface TemplateService {

    Map<String, String> getAllTemplateImages();

    void preRenderAllTemplates();
}
