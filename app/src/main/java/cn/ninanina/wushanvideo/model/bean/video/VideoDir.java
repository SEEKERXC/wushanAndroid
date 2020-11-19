package cn.ninanina.wushanvideo.model.bean.video;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(of = "id")
public class VideoDir {
    private Long id;
    private String name;
    private Long createTime;
    private Long updateTime;
    private Integer count;
    private List<VideoDetail> collectedVideos;
}
