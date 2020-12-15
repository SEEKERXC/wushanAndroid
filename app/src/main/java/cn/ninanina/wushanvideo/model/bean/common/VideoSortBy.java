package cn.ninanina.wushanvideo.model.bean.common;

public enum VideoSortBy {
    DEFAULT("default", "默认排序"),
    VIEWED("viewed", "播放多"), //此乃视频总播放次数
    COLLECT("collected", "收藏多"),
    DOWNLOAD("downloaded", "下载多"),
    COMMENT("commentNum", "评论多"),
    PLAY("play", "播放次数"), //此乃当前用户观看次数
    SIZE("size", "文件大小"),
    NAME("name", "文件名称"),
    DURATION("duration", "视频时长"),
    UPDATE_TIME("updateTime", "更新时间");


    VideoSortBy(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private final String code;
    private final String msg;

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
