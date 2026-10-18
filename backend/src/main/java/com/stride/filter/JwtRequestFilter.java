package com.stride.filter;

import com.stride.exception.BusinessException;
import com.stride.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证 + 鉴权拦截器。
 *
 * 【名字的说明】
 * 这个类叫 Filter 但实际实现的是 HandlerInterceptor（拦截器），名字有点误导。
 * 拦截器 vs 过滤器的区别是经典面试题：
 *   Filter     属于 Servlet 规范，在 DispatcherServlet 之前执行，
 *              拿不到"要调用哪个 Controller 方法"的信息；
 *   Interceptor 属于 Spring MVC，在 DispatcherServlet 之后、Controller 之前执行，
 *              可以拿到 HandlerMethod，适合做基于注解的权限判断。
 */
@Component
public class JwtRequestFilter implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 放行跨域预检请求（OPTIONS）。
        // 浏览器在发 PUT/DELETE 或带自定义头的请求前会先发 OPTIONS 探测，
        // 这个请求不带 Authorization，如果不放行就会被拦成 401，导致跨域失败。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }

        // 没有携带 Token → 401 未认证
        if (jwt == null || jwt.isBlank()) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        // 【修复点：先解析，把 JWT 异常交给全局异常处理器 → 返回 401 而不是 500】
        // 原来的写法是 extractUsername() 直接调用，且没有任何 try-catch，
        // Token 过期时抛出的 ExpiredJwtException 会一路冒泡变成 500。
        // 这里显式捕获并抛 BusinessException(401)，语义才正确。
        String username;
        Integer role;
        try {
            username = jwtUtil.extractUsername(jwt);
            role = jwtUtil.extractRole(jwt);
        } catch (JwtException | IllegalArgumentException e) {
            // 过期、签名不匹配、格式错误都会走到这里
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }

        if (username == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }

        // ============ 鉴权：基于角色的访问控制（RBAC）============
        // 这是原来完全缺失的一环。原代码只校验了 Token 有效性，
        // 从不读取 role，导致普通用户也能访问管理端接口（越权漏洞）。
        if (requiresAdmin(request) && (role == null || role != 1)) {
            // 403 而不是 401：
            //   401 = 未认证（你是谁我不知道）→ 前端应跳登录页
            //   403 = 已认证但无权限（我知道你是谁，但你不许访问）→ 前端应提示无权限
            throw new BusinessException(403, "无权限访问该资源");
        }

        // 把用户信息放进 request，后续 Controller 可直接取用，避免重复解析 Token
        request.setAttribute("currentUsername", username);
        request.setAttribute("currentUserId", jwtUtil.extractUserId(jwt));
        request.setAttribute("currentRole", role);

        return true;
    }

    /**
     * 判断这个请求是否需要管理员角色。
     *
     * 【设计说明】
     * 这里用"路径规则"而不是给每个接口加注解，是因为项目规模小、规则简单。
     * 如果接口多了，更推荐自定义注解（如 @RequireRole(1)）+ 在拦截器里
     * 通过 handler 参数拿到 HandlerMethod 再读取注解，那样更灵活也更清晰。
     */
    private boolean requiresAdmin(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 查询全部用户列表：仅管理员
        if ("/api/users".equals(uri) && "GET".equalsIgnoreCase(method)) {
            return true;
        }
        // 修改、删除指定用户：仅管理员
        if (uri.startsWith("/api/users/") && uri.length() > "/api/users/".length()
                && ("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))) {
            return true;
        }
        return false;
    }
}
