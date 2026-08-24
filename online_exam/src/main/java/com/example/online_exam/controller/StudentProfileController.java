package com.example.online_exam.controller;

import com.example.online_exam.controller.dto.MyExamAnswerView;
import com.example.online_exam.controller.dto.MyExamSummaryView;
import com.example.online_exam.controller.dto.MyExamView;
import com.example.online_exam.controller.dto.ResponseDTO;
import com.example.online_exam.entity.User;
import com.example.online_exam.service.StudentProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    public StudentProfileController(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;
    }

    // 获取我的考试列表
    @GetMapping("/me/exams")
    public ResponseDTO<List<MyExamView>> myExams(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"student".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        return ResponseDTO.success(studentProfileService.listMyExams(loginUser.getId()));
    }

    // 获取我的考试详情（summary + detail）
    @GetMapping("/me/exams/{examId}")
    public ResponseDTO<MyExamDetailResponse> myExamDetail(@PathVariable Integer examId, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"student".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        MyExamDetailResponse res = new MyExamDetailResponse();
        res.setSummary(studentProfileService.getMyExamSummary(loginUser.getId(), examId));
        res.setDetail(studentProfileService.getMyExamDetail(loginUser.getId(), examId));
        return ResponseDTO.success(res);
    }

    public static class MyExamDetailResponse {
        private MyExamSummaryView summary;
        private List<MyExamAnswerView> detail;

        public MyExamSummaryView getSummary() { return summary; }
        public void setSummary(MyExamSummaryView summary) { this.summary = summary; }

        public List<MyExamAnswerView> getDetail() { return detail; }
        public void setDetail(List<MyExamAnswerView> detail) { this.detail = detail; }
    }
}