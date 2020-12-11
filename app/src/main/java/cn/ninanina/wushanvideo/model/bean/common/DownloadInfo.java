package cn.ninanina.wushanvideo.model.bean.common;

import cn.ninanina.wushanvideo.model.bean.video.VideoDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * 正在下载的实时信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadInfo {
    public static final int running = 0;
    public static final int waiting = 1;
    public static final int pause = 2;
    public static final int error = 3;

    private VideoDetail video; //关联video
    private long taskId; //下载任务id
    private String url; //下载链接
    private String path; //下载路径
    private String fileName; //文件名字
    private String speed; //下载速度，格式如：2.2MB/s
    private String progress; //下载进度，格式如：24.5MB/48.9MB
    private int percentage; //下载进度百分比
    private int status; //下载状态
}
