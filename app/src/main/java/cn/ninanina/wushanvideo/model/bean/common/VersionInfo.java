package cn.ninanina.wushanvideo.model.bean.common;

import lombok.Data;

@Data
public class VersionInfo {
    private Long id;
    private String versionCode;
    private String updateInfo;
    private Long updateTime;
    private String appUrl;
}
