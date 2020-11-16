package cn.ninanina.wushanvideo.model.bean.video;

import cn.ninanina.wushanvideo.model.bean.common.User;
import lombok.Data;

@Data
public class Comment {
    private Long id;
    private String content;
    private Long time;
    private Integer approved;
    private Long parentId;
    private User user;
}
