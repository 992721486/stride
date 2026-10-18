package com.stride.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.stride.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 工具类：负责签发和解析 Token。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 签发 Token。
     *
     * JWT 由三段组成，用 . 分隔：
     *   header.payload.signature
     *   ① header    算法信息，如 {"alg":"HS256","typ":"JWT"}
     *   ② payload   载荷，存放 claims（这里放了 role、userId、sub、iat、exp）
     *   ③ signature 用密钥对前两段做的签名，防止被篡改
     *
     * 【重要认知：JWT 不是加密的，只是 Base64 编码】
     * 任何人拿到 Token 都能把中间那段 Base64 解码，看到里面所有内容。
     * 所以绝对不要在 JWT 里放密码、身份证号这类敏感信息。
     * 签名只保证"内容没被篡改"，不保证"内容不被看到"。
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("userId", user.getId());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从 Token 中取出角色。
     *
     * 【为什么这个方法是修越权漏洞的前提】
     * Token 里本来就带着 role（generateToken 里放进去的），
     * 但原来的 JwtRequestFilter 只校验了 Token 是否有效，从来没读它。
     * 有了这个方法，拦截器才能判断"这个用户是不是管理员"。
     *
     * 注意 claims 里的数字默认会被反序列化成 Integer，
     * 直接强转其他类型会抛 ClassCastException。
     */
    public Integer extractRole(String token) {
        Claims claims = extractAllClaims(token);
        Object role = claims.get("role");
        return role == null ? null : Integer.valueOf(role.toString());
    }

    public Integer extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        return userId == null ? null : Integer.valueOf(userId.toString());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
