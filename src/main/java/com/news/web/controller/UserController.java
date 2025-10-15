package com.news.web.controller;

import com.news.web.pojo.Result;
import com.news.web.pojo.User;
import com.news.web.service.UserService;
import com.news.web.utils.JwtUtil;
import com.news.web.utils.Md5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/register")
    public Result<?> register(String username, String password) {
        User user = userService.selectByName(username);
        if (user != null) {
            return Result.error("用户名已存在");
        }

        userService.insertUser(username, password);
        return Result.success();
    }

    @RequestMapping("/login")
    public Result<?> login(String username, String password) {
        User user = userService.selectByName(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!Md5Util.getMD5String(password).equals(user.getPassword())) {
            return Result.error("密码不正确");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("userId", user.getUserId());
        map.put("refreshTime", LocalDateTime.now().toString()); // 加入刷新时间用于控制缓存更新

        String token = JwtUtil.genToken(map);

        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set(token, token, 60, TimeUnit.MINUTES); // 设置 token 过期时间为60分钟

        return Result.success(token);
    }

    @RequestMapping("/userInfo")
    public Result<?> userInfo(@RequestHeader("authorization") String token) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        String username = (String) map.get("username");

        // 刷新 Token 生存时间（可选）
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set(token, token, 60, TimeUnit.MINUTES); // 设置 token 过期时间为60分钟


        User user = userService.selectByName(username);
        return Result.success(user);
    }

    @RequestMapping("/updatePwd")
    public Result<?> updatePwd(@RequestBody Map<String, String> params, @RequestHeader("authorization") String token) {
        String oldPwd = params.get("old_pwd");
        String new_pwd = params.get("new_pwd");
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer userId = (Integer) map.get("userId");
        String username = (String) map.get("username");
        User user1 = userService.selectByName(username);
        if (!Md5Util.getMD5String(oldPwd).equals(user1.getPassword())) {
            return Result.error("原密码错误");
        }
        User user = new User();
        user.setUserId(userId);
        user.setPassword(Md5Util.getMD5String(new_pwd));
        userService.updatePwd(user);
        return Result.success();
    }
}