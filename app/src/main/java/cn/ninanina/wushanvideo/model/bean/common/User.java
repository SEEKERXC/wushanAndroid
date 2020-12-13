package cn.ninanina.wushanvideo.model.bean.common;

import java.io.Serializable;

import lombok.Data;

@Data
public class User implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String gender;
    private int age;
    private Boolean straight;
    private String photo;
    private Long registerTime;
    private Long lastLoginTime;
}
