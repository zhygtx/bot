package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 插入用户
      * @param user 用户对象
     */
    @RequestMapping("/insertUser")
    public Result<String> insertUser(User user) {
        if (userService.isExist(user.getAccount())){
            return Result.error("该账号已存在");
        }
        userService.insertUser(user);
        return Result.success("注册成功");
    }

    /**
     * 登录
     * @param account 账号
     * @param pwd 密码
     */
    @RequestMapping("/login")
    public Result<String> login(String account, String pwd) {
        if (!userService.isExist(account)){
            return Result.error("该账号不存在");
        }
        if (!userService.login(account, pwd)){
            return Result.error("密码错误");
        }
        return Result.success("登录成功");
    }

    /**
     * 修改用户信息
     * @param user 用户对象
     */
    @RequestMapping("/update")
    public Result<String> update(User user) {
        userService.updateUser(user);
        return Result.success("修改成功");
    }

    /**
     * 修改密码
     * @param account 账号
     * @param oldPwd 旧密码
     * @param newPwd 新密码
     */
    @RequestMapping("/updatePwd")
    public Result<String> updatePwd(String account, String oldPwd, String newPwd) {
        if (!userService.login(account, oldPwd)){
            return Result.error("旧密码错误");
        }
        User user = new User();
        user.setAccount(account);
        user.setPwd(newPwd);
        userService.updatePwd(user);
        return Result.success("修改成功");
    }

    @RequestMapping("/updateEmail")
    public Result<String> updateEmail(User user) {
        user.setAccount(user.getAccount());
        user.setEmail(user.getEmail());
        userService.updateEmail(user);
        return Result.success("修改成功");
    }
}
