package cn.ninanina.wushanvideo.model.bean.common;

import lombok.Data;

@Data
public class Feedback {
    private Long id;
    private User user;
    private String content;
    private Long time;
}
