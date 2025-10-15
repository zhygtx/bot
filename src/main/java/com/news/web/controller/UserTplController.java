package com.news.web.controller;


import com.news.web.pojo.Result;
import com.news.web.pojo.UserTpl;
import com.news.web.service.UserTplService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userTpl")
public class UserTplController {

    @Autowired
    private UserTplService userTplService;

    @RequestMapping("/insert")
    public Result<String> insert(@RequestBody UserTpl userTpl){
        if (userTplService.hasUserTpl(userTpl.getId())){
            return Result.error("您已添加该模板");
        }
        userTplService.insert(userTpl);
        return Result.success("添加成功");
    }

    @RequestMapping("/selectByUserId")
    public Result<UserTpl> selectByUserId(Long userId){
        UserTpl userTpl = userTplService.selectByUserId(userId);
        return Result.success(userTpl);
    }

    @RequestMapping("/update")
    public Result<String> update(@RequestBody UserTpl userTpl){
        userTplService.update(userTpl);
        return Result.success("修改成功");
    }

    @RequestMapping("/delete")
    public Result<String> delete(@RequestBody UserTpl userTpl){
        userTplService.delete(userTpl);
        return Result.success("删除成功");
    }
}
