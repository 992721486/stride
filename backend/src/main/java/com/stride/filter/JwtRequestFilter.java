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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 要实现 HandlerInterceptor 接口，必须重写 preHandle 方法：返回 true 放行，抛异常拦截

        // 放行跨域预检（OPTIONS）：浏览器发 PUT/DELETE 前会先探测一次，这个请求不带 Token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        final String authorization = request.getHeader("Authorization");
        String jwt = null;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            jwt = authorization.substring(7);
        }

        // 没有携带 Token → 401 未认证
        if (jwt == null || jwt.isBlank()) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        // 解析失败（过期 / 签名不匹配 / 格式错）统一转 401：
        // 若不捕获，异常冒泡出去就是 500，前端会以为服务器挂了
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
        // role == null || 不能省：Integer 与 1 比较会自动拆箱，null 拆箱会空指针
        if (requiresAdmin(request) && (role == null || role != 1)) {
            // 403 而不是 401：401 = 不知道你是谁（去登录）；403 = 知道你是谁，但你不许进
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
