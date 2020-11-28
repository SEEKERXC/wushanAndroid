package cn.ninanina.wushanvideo.model.bean.video;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class Tag implements Serializable {
    private Integer id;
    private String tag;
    private String tagZh;
    private Integer videoCount;
}
