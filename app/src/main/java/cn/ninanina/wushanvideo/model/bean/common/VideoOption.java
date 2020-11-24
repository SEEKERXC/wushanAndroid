package cn.ninanina.wushanvideo.model.bean.common;

import lombok.Data;

@Data
public class VideoOption {
    int resourceId;
    String name;

    public VideoOption(int resourceId, String name) {
        this.resourceId = resourceId;
        this.name = name;
    }
}
