package com.example.online_exam.controller.dto;

import lombok.Data;

// 统一JSON响应体
@Data
public class ResponseDTO<T> {
    private int code;       // 状态码：200成功，401未登录，403无权限，500失败
    private String msg;     // 提示信息
    private T data;         // 响应数据

    // 成功返回
    public static <T> ResponseDTO<T> success(T data) {
        ResponseDTO<T> dto = new ResponseDTO<>();
        dto.setCode(200);
        dto.setMsg("success");
        dto.setData(data);
        return dto;
    }

    // 未登录
    public static <T> ResponseDTO<T> unAuth() {
        ResponseDTO<T> dto = new ResponseDTO<>();
        dto.setCode(401);
        dto.setMsg("未登录");
        dto.setData(null);
        return dto;
    }

    // 无权限
    public static <T> ResponseDTO<T> forbidden() {
        ResponseDTO<T> dto = new ResponseDTO<>();
        dto.setCode(403);
        dto.setMsg("无操作权限");
        dto.setData(null);
        return dto;
    }

    // 失败
    public static <T> ResponseDTO<T> fail(String msg) {
        ResponseDTO<T> dto = new ResponseDTO<>();
        dto.setCode(500);
        dto.setMsg(msg);
        dto.setData(null);
        return dto;
    }
}