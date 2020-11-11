package cn.ninanina.wushanvideo.model.bean.video;

import java.io.Serializable;

import lombok.Data;

@Data
public class Tag implements Serializable {
    private int id;
    private String tag;
    private String tagZh;
    private int videoCount;
}
