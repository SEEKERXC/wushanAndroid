package cn.ninanina.wushanvideo.model.bean.common;

public enum VideoDuration {
    ALL("all", "所有时长"),
    SHORT("short", "0-30分钟"),
    MIDDLE("middle", "30-60分钟"),
    LONG("long", "> 60分钟");

    VideoDuration(String code, String msg) {
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
