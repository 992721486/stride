package com.stride.controller;

import com.stride.common.Result;
import com.stride.entity.User;
import com.stride.exception.BusinessException;
import com.stride.service.AuthService;
import com.stride.service.UserService;
import com.stride.vo.LoginVO;
import com.stride.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户接口层。
 *
 * 【本次改动要点】
 *   ① 所有接口统一返回 Result<T>，不再返回裸实体或裸字符串；
 *   ② 返回 UserVO 而不是 User，密码不再泄露到前端；
 *   ③ 补齐 PUT / DELETE 接口（原来只有 GET/POST，前端调用会返回 405）；
 *   ④ 不再写 try-catch，异常交给 GlobalExceptionHandler 统一处理。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * 注册。
     * POST /api/users/register
     *
     * 不需要 Token（在 WebConfig 的 excludePathPatterns 里放行了）。
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody User user) {
        User saved = userService.register(user);
        return Result.success("注册成功", UserVO.from(saved));
    }

    /**
     * 登录。
     * POST /api/users/login
     *
     * 返回 { token, user }，前端一次拿到全部信息，
     * 不需要像原来那样登录后再发一次请求去查用户详情。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody User user) {
        AuthService.AuthResult result = authService.authenticate(user.getUsername(), user.getPassword());
        UserVO userVO = UserVO.from(result.user());
        return Result.success("登录成功", new LoginVO(result.token(), userVO));
    }

    /**
     * 查询当前登录用户信息。
     * GET /api/users/me
     *
     * 用 /me 而不是 /{username} 的理由：
     *   ① 用户信息从 Token 里取，不需要前端传用户名，无法查询别人，更安全；
     *   ② 语义更符合 REST（"当前用户"是一个固定资源）。
     * 原来的 GET /api/users/{username} 允许任何登录用户查询别人的信息。
     */
    @GetMapping("/me")
    public Result<UserVO> me(HttpServletRequest request) {
        String username = (String) request.getAttribute("currentUsername");
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return Result.success(UserVO.from(user));
    }

    /**
     * 查询全部用户（仅管理员，拦截器已做角色校验）。
     * GET /api/users
     */
    @GetMapping
    public Result<List<UserVO>> getAllUsers() {
        List<UserVO> list = userService.findAllUsers()
                .stream()
                .map(UserVO::from)
                .toList();
        return Result.success(list);
    }

    /**
     * 修改用户信息（仅管理员）。
     * PUT /api/users/{id}
     *
     * 【这个接口原来是缺失的，前端调用直接返回 405 Method Not Allowed】
     */
    @PutMapping("/{id}")
    public Result<UserVO> updateUser(@PathVariable Integer id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return Result.success("修改成功", UserVO.from(updated));
    }

    /**
     * 删除用户（仅管理员）。
     * DELETE /api/users/{id}
     *
     * 【同样是原来缺失的接口，Admin.vue 的删除按钮因此一直失败】
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Integer id, HttpServletRequest request) {
        // 防止管理员误删自己
        Integer currentUserId = (Integer) request.getAttribute("currentUserId");
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new BusinessException(400, "不能删除当前登录的账号");
        }
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }
}
