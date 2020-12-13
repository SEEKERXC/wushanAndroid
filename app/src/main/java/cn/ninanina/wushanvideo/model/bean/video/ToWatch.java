package cn.ninanina.wushanvideo.model.bean.video;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")

public class ToWatch {
    private Long id;
    private Long videoId;
    private Long userId;
    private Long addTime;
}
