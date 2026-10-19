package com.stride.vo;
import com.stride.entity.User;
public class UserVO{
    private Integer id;
    private String username;
    private String email;
    private Integer role;

    public static UserVO from(User user) {
        if(user == null) {
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