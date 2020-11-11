package cn.ninanina.wushanvideo.model.bean.video;

import java.util.List;

import lombok.Data;

@Data
public class VideoDetail {
    private long id;
    private String title;
    private String url;
    private String src;
    private String coverUrl;
    private String duration;
    private int viewed;
    private Integer approved;
    private Boolean valid;
    private List<Tag> tags;
}
