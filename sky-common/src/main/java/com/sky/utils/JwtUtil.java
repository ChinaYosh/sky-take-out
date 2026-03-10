package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    // 使用固定的密钥 - 256 位密钥
    private static final String SECRET_KEY_STRING = "sky-take-out-jwt-secret-key-2026";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 jwt
     * 使用 Hs256 算法，私匙使用安全生成的密钥
     *
     * @param ttlMillis jwt 过期时间 (毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(long ttlMillis, Map<String, Object> claims) {
        // 生成 JWT 的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 设置 jwt 的 body
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SECRET_KEY)
                .setExpiration(exp)
                .compact();
    }

    /**
     * Token 解密
     *
     * @param token 加密后的 token
     * @return
     */
    public static Claims parseJWT(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}