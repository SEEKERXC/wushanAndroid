package cn.ninanina.wushanvideo.model.bean.video;

import android.content.Intent;

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
    private Integer audience;
    private Integer collected;
    private Integer downloaded;
    private Integer liked;
    private Integer disliked;
    private Long size;
    private List<Tag> tags;
}
