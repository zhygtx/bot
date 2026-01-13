package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.User;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import com.example.demo.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final EmailService emailService;
    private final UserService userService;
    private final JWTUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, EmailService emailService, JWTUtil jwtUtil) {
        this.userService = userService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 插入用户
      * @param user 用户对象
     */
    @RequestMapping("/insertUser")
    public Result<String> insertUser(User user) {
        if (userService.isExistByAccount(user.getAccount())){
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
    public Result<Map<String, Object>> login(String account, String pwd) {
        if (!userService.isExistByAccount(account)){
            return Result.error("该账号不存在");
        }
        if (!userService.login(account, pwd)){
            return Result.error("密码错误");
        }

        // 获取用户信息
        User user = userService.selectByAccount(account);
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getAccount());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getName());
        result.put("account", user.getAccount());

        return Result.success("登录成功", result);
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

    /**
     * 修改邮箱
     * @param user 用户对象
     */
    @RequestMapping("/updateEmail")
    public Result<String> updateEmail(User user) {
        user.setAccount(user.getAccount());
        user.setEmail(user.getEmail());
        userService.updateEmail(user);
        return Result.success("修改成功");
    }

    /**
     * 忘记密码
     * @param account 账号
     * @param email 邮箱
     * @param newPwd 新密码
     * @param code 验证码
     */
    @RequestMapping("/retrievePwd")
    public Result<String> retrievePwd(String account, String email, String newPwd, String code) {
        if (!userService.isExistByAccount(account)) return Result.error("该账号不存在");
        if (!userService.selectByAccount(account).getEmail().equals(email)) return Result.error("该邮箱并未与此账号绑定");
        if (!emailService.verifyCode(email, code)) return Result.error("验证码错误");
        if (userService.login(account, newPwd)) return Result.error("新密码不能与旧密码相同");
        User user = new User();
        user.setAccount(account);
        user.setPwd(newPwd);
        userService.updatePwd(user);
        return Result.success("修改成功");
    }

    /**
    * 退出登录
    * @param userId 用户ID
    */
    @RequestMapping("/logout")
    public Result<String> logout(String userId) {
        // 从Redis中删除token
        jwtUtil.deleteToken(userId);
        return Result.success("退出成功");
    }
}
