package com.stride.vo;

import com.stride.entity.User;

/**
 * 返回给前端的用户对象（VO = View Object）。
 *
 * 【为什么不能直接返回 User 实体】
 * User 里有 password 字段。如果 Controller 直接返回实体，
 * Jackson 会把密码一起序列化成 JSON 发给前端，甚至被前端存进 localStorage。
 * 一旦有人打开浏览器 F12 看响应，就能看到所有人的密码（原来是明文）。
 *
 * 解决办法：单独定义一个"给前端看的对象"，只包含该暴露的字段。
 * 这就是 DTO / VO 层的意义——实体对应数据库，VO 对应接口契约，两者不该混用。
 */
public class UserVO {

    private Integer id;
    private String username;
    private String email;
    private Integer role;

    /**
     * 把实体转成 VO。
     *
     * 【注意 password 没有被复制过来】——这是这个类存在的全部意义。
     */
    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        return vo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
}
