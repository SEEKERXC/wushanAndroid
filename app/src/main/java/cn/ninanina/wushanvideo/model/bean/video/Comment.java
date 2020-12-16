package cn.ninanina.wushanvideo.model.bean.video;

import java.io.Serializable;

import cn.ninanina.wushanvideo.model.bean.common.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class Comment implements Serializable {
    private Long id;
    private String content;
    private Long time;
    private Integer approve;
    private Integer disapprove;
    private Boolean approved;
    private Boolean disapproved;
    private Long parentId;
    private Comment parent;
    private User user;
}
