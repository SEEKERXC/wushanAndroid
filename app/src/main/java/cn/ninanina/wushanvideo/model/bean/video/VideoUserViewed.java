package cn.ninanina.wushanvideo.model.bean.video;

import lombok.Data;

@Data
public class VideoUserViewed {
    private long id;
    private long videoId;
    private long time;
    private int viewCount;
    private long watchTime;
}
