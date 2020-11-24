package cn.ninanina.wushanvideo.model.bean.video;

import cn.ninanina.wushanvideo.model.bean.common.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(of = "id")
public class Playlist {
    private Long id;
    private String name;
    private String cover;
    private Long createTime;
    private Long updateTime;
    private Integer count;
    private Boolean isPublic;
    private User user;
}
