package cn.ninanina.wushanvideo.model.bean.common;

import lombok.Data;

@Data
public class Option {
    int resourceId;
    String name;

    public Option(int resourceId, String name) {
        this.resourceId = resourceId;
        this.name = name;
    }
}
