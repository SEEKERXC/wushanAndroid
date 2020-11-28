package cn.ninanina.wushanvideo.model.bean.video;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class VideoDetail implements Serializable {
    private Long id;
    private String title;
    private String titleZh;
    private String url;
    private String src;
    private String coverUrl;
    private String duration;
    private Long updateTime;
    private Integer viewed;
    private Integer collected;
    private Integer disliked;
    private List<Tag> tags;
}
