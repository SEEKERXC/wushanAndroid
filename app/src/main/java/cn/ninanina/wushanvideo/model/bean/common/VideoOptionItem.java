package cn.ninanina.wushanvideo.model.bean.common;

import lombok.Data;

@Data
public class VideoOptionItem {
    int resourceId;
    String name;

    public VideoOptionItem(int resourceId, String name) {
        this.resourceId = resourceId;
        this.name = name;
    }
}
