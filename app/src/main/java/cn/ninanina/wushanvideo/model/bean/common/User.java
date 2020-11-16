package cn.ninanina.wushanvideo.model.bean.common;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String gender;
    private int age;
    private Long registerTime;
    private Long lastLoginTime;
}
