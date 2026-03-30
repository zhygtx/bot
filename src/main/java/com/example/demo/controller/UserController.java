package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.User;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import com.example.demo.util.AuthUtil;
import com.example.demo.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final EmailService emailService;
    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final AuthUtil authUtil;

    public UserController(UserService userService, EmailService emailService, JWTUtil jwtUtil, AuthUtil authUtil) {
        this.userService = userService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.authUtil = authUtil;
    }

    /**
     * 插入用户
      * @param user 用户对象
     */
    @PostMapping("/insertUser")
    public Result<String> insertUser(@RequestBody User user) {
        if (userService.isExistByAccount(user.getAccount())){
            return Result.error( 400, "该账号已存在");
        }
        userService.insertUser(user);
        return Result.success(null, null);
    }

    /**
     * 登录
     * @param account 账号
     * @param pwd 密码
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(String account, String pwd) {
        if (!userService.isExistByAccount(account)){
            return Result.error(400,"该账号不存在");
        }
        if (!userService.login(account, pwd)){
            return Result.error(400,"密码错误");
        }

        // 获取用户信息
        User user = userService.selectByAccount(account);
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getAccount(), user.getName());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getName());
        result.put("account", user.getAccount());

        return Result.success(null,result);
    }

    /**
     * 获取当前用户信息
      * @param request HttpServletRequest对象
     */
    @GetMapping("/info")
    public Result<User> getCurrentUserInfo(HttpServletRequest request) {
        User user = userService.selectById(authUtil.getCurrentUserId(request));
        if (user == null) {
            return Result.error(400,"用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 修改用户信息
     * @param user 用户对象
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        return userService.updateUser(user) ==1 ? Result.success():Result.error(400,"修改失败");
    }

    /**
     * 修改密码
     * @param oldPwd 旧密码
     * @param newPwd 新密码
     */
    @PutMapping("/updatePwd")
    public Result<String> updatePwd(HttpServletRequest request, String oldPwd, String newPwd) {
        String account = authUtil.getCurrentUserAccount(request);
        if (!userService.login(account, oldPwd)){
            return Result.error(400,"旧密码错误");
        }
        User user = new User();
        user.setId(authUtil.getCurrentUserId(request));
        user.setPwd(newPwd);
        userService.updatePwd(user);
        return Result.success();
    }

    /**
     * 修改邮箱
     * @param user 用户对象
     */
    @PutMapping("/updateEmail")
    public Result<String> updateEmail(@RequestBody User user) {
        user.setAccount(user.getAccount());
        user.setEmail(user.getEmail());
        userService.updateEmail(user);
        return Result.success();
    }

    /**
     * 忘记密码
     * @param account 账号
     * @param email 邮箱
     * @param newPwd 新密码
     * @param code 验证码
     */
    @PutMapping("/retrievePwd")
    public Result<String> retrievePwd(String account, String email, String newPwd, String code) {
        if (!userService.isExistByAccount(account)) return Result.error(400,"该账号不存在");
        if (!userService.selectByAccount(account).getEmail().equals(email)) return Result.error(400,"该邮箱并未与此账号绑定");
        if (!emailService.verifyCode(email, code)) return Result.error(400,"验证码错误");
        if (userService.login(account, newPwd)) return Result.error(400,"新密码不能与旧密码相同");
        User user = userService.selectByAccount(account);
        user.setId(user.getId());
        user.setPwd(newPwd);
        userService.updatePwd(user);
        return Result.success();
    }

    /**
    * 退出登录
    * @param userId 用户ID
    */
    @PostMapping("/logout")
    public Result<String> logout(String userId) {
        // 从Redis中删除token
        jwtUtil.deleteToken(userId);
        return Result.success();
    }
}