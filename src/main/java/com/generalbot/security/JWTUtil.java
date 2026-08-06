package com.generalbot.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JWTUtil {

    @Value("${jwt.secret:my-super-secret-jwt-key-change-in-production}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private Long expiration;

    // 发行人
    private static final String ISSUER = "general-bot";

    private final RedisTemplate<String, Object> redisTemplate;

    public JWTUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 生成JWT令牌
    public String generateToken(String userId, String account, String userName) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        String token = JWT.create()
                .withIssuer(ISSUER)
                .withClaim("userId", userId)
                .withClaim("account", account)
                .withClaim("userName", userName)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(algorithm);

        // 将token存入Redis，设置过期时间与JWT过期时间一致
        redisTemplate.opsForValue().set(
                "token:" + userId,
                token,
                expiration,
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
        token = token.replace("Bearer ", "");
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
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
        if (token == null) {
            return false;
        }
        token = token.replace("Bearer ", "");
        String redisToken = (String) redisTemplate.opsForValue().get("token:" + userId);
        return token.equals(redisToken);
    }

    // 从令牌中获取用户ID
    public String getUserIdFromToken(String token) {
        token = token.replace("Bearer ", "");
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("userId").asString();
    }

    // 从令牌中获取账号
    public String getAccountFromToken(String token) {
        token = token.replace("Bearer ", "");
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("account").asString();
    }

    // 从令牌中获取用户名
    public String getUserNameFromToken(String token) {
        token = token.replace("Bearer ", "");
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("userName").asString();
    }

    // 检查令牌是否过期
    public boolean isTokenExpired(String token) {
        token = token.replace("Bearer ", "");
        DecodedJWT decodedJWT = JWT.decode(token);
        Date expiresAt = decodedJWT.getExpiresAt();
        return expiresAt.before(new Date());
    }
}