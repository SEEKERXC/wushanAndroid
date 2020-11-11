package cn.ninanina.wushanvideo.model.bean;

import lombok.Data;

@Data
public class Result<T> {
    private String rspCode;
    private String rspMsg;
    private T data;
}
