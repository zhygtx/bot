package com.example.demo.ai.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.CodeService;
import com.example.demo.pojo.entity.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/code")
public class CodeController {

    private final CodeService codeService;

    public CodeController(CodeService codeService) {
        this.codeService = codeService;
    }

    @GetMapping("{messageId}")
    public Result<?> getCode(@PathVariable("messageId") String messageId) {
        QueryWrapper<Code> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("message_id", messageId);
        List<Code> codeList = codeService.getBaseMapper().selectList(queryWrapper);
        return Result.success(null, codeList);
    }
}
