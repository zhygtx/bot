package com.example.demo.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtil {

    // JWT密钥，生产环境中应从配置文件获取
    private static final String SECRET = "your-secret-key-change-this-in-production";
    // 过期时间（2小时）
    private static final long EXPIRE_TIME = 2 * 60 * 60 * 1000;
    // 发行人
    private static final String ISSUER = "general-bot";

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public JWTUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 生成JWT令牌
    public String generateToken(String userId, String account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("account", account);

        Algorithm algorithm = Algorithm.HMAC256(SECRET);

        String token = JWT.create()
                .withIssuer(ISSUER)
                .withClaim("userId", userId)
                .withClaim("account", account)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(algorithm);

        redisTemplate.opsForValue().set(
                "token:" + userId,
                token,
                EXPIRE_TIME,
                TimeUnit.MILLISECONDS
        );

        return token;
    }

    /**
     * 从Redis中删除token
     * @param userId 用户ID
     */
    public void deleteToken(String userId) {
        redisTemplate.delete("token:" + userId);
    }

    /**
     * 验证JWT令牌
     * @param token 令牌
     * @return 是否验证通过
     */
    public boolean verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 验证token是否在Redis中存在
     * @param userId 用户ID
     * @param token 令牌
     * @return 是否存在
     */
    public boolean isTokenInRedis(String userId, String token) {
        String redisToken = redisTemplate.opsForValue().get("token:" + userId);
        return token != null && token.equals(redisToken);
    }

    // 从令牌中获取用户ID
    public String getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("userId").asString();
    }

    // 从令牌中获取账号
    public String getAccountFromToken(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("account").asString();
    }

    // 检查令牌是否过期
    public boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        Date expiresAt = decodedJWT.getExpiresAt();
        return expiresAt.before(new Date());
    }
}