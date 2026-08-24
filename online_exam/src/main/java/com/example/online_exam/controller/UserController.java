package com.example.online_exam.controller;

import com.example.online_exam.controller.dto.LoginDTO;
import com.example.online_exam.controller.dto.ResponseDTO;
import com.example.online_exam.entity.User;
import com.example.online_exam.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/hello")
    public ResponseDTO<String> hello() {
        return ResponseDTO.success("User Controller OK");
    }

    // 注册
    @PostMapping("/register")
    public ResponseDTO<Void> register(@RequestBody User user) {
        try {
            userService.register(user);
            return ResponseDTO.success(null);
        } catch (Exception e) {
            return ResponseDTO.fail("注册失败：" + e.getMessage());
        }
    }

    // 登录（写入 Session）
    @PostMapping("/login")
    public ResponseDTO<User> login(@RequestBody LoginDTO dto, HttpSession session) {
        User loginUser = userService.login(dto.getUsername(), dto.getPassword());
        if (loginUser == null) {
            return ResponseDTO.fail("用户名或密码错误");
        }
        session.setAttribute("loginUser", loginUser);
        return ResponseDTO.success(loginUser);
    }

    // 当前用户
    @GetMapping("/me")
    public ResponseDTO<User> me(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        return ResponseDTO.success(loginUser);
    }

    // 退出
    @PostMapping("/logout")
    public ResponseDTO<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseDTO.success(null);
    }
}