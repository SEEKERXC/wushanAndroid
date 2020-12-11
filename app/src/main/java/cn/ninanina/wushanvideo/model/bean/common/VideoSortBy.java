package cn.ninanina.wushanvideo.model.bean.common;

public enum VideoSortBy {
    DEFAULT("default", "默认排序"),
    PLAY("viewed", "播放多"),
    COLLECT("collected", "收藏多"),
    DOWNLOAD("downloaded", "下载多"),
    COMMENT("commentNum", "评论多");

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
