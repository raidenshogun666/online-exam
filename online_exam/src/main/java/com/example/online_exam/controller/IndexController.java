package com.example.online_exam.controller;

import com.example.online_exam.controller.dto.ResponseDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class IndexController {
    @GetMapping("/")
    public ResponseDTO<String> index(HttpSession session) {
        if (session.getAttribute("loginUser") == null) {
            return ResponseDTO.unAuth(); // 未登录返回401
        }
        return ResponseDTO.success("已登录，跳转首页"); // 前端自行处理路由跳转
    }
}