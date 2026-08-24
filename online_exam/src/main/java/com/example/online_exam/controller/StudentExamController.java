package com.example.online_exam.controller;

import com.example.online_exam.controller.dto.ResponseDTO;
import com.example.online_exam.controller.dto.TakeExamQuestionView;
import com.example.online_exam.entity.User;
import com.example.online_exam.exception.RepeatSubmitNotAllowedException;
import com.example.online_exam.service.StudentExamService;
import com.example.online_exam.util.ExamTimeUtil;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StudentExamController {

    private final StudentExamService studentExamService;

    public StudentExamController(StudentExamService studentExamService) {
        this.studentExamService = studentExamService;
    }

    // 获取考试题目（用于答题，返回JSON）
    @GetMapping("/exams/{examId}/take")
    public ResponseDTO<TakeExamResponse> takeExam(@PathVariable Integer examId, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"student".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        StudentExamService.ExamTimeRange range = studentExamService.getExamTimeRange(examId);
        ExamTimeUtil.Status status = ExamTimeUtil.getStatus(range.getStartTime(), range.getEndTime(), LocalDateTime.now());

        if (status != ExamTimeUtil.Status.IN_PROGRESS) {
            TakeExamResponse res = new TakeExamResponse();
            res.setExamId(examId);
            res.setStatus(status.name());
            res.setStartTime(range.getStartTime());
            res.setEndTime(range.getEndTime());
            res.setQuestions(null);
            return ResponseDTO.fail("考试未开始/已结束");
        }



        List<TakeExamQuestionView> questions = studentExamService.getExamQuestionsForTaking(examId);
        TakeExamResponse res = new TakeExamResponse();
        res.setExamId(examId);
        res.setStatus(status.name());

        res.setStartTime(range.getStartTime());
        res.setEndTime(range.getEndTime());
        res.setDuration(range.getDuration());

        res.setQuestions(questions);
        return ResponseDTO.success(res);
    }

    // 提交考试答案（返回JSON）
    @PostMapping("/exams/{examId}/submit")
    public ResponseDTO<SubmitExamResponse> submitExam(@PathVariable Integer examId,
                                                      @RequestBody Map<String, String> answers, // 接收JSON格式答案
                                                      HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"student".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        // 校验考试时间
        StudentExamService.ExamTimeRange range = studentExamService.getExamTimeRange(examId);
        ExamTimeUtil.Status status = ExamTimeUtil.getStatus(range.getStartTime(), range.getEndTime(), LocalDateTime.now());
        if (status != ExamTimeUtil.Status.IN_PROGRESS) {
            return ResponseDTO.fail("考试未开始/已结束，无法提交");
        }

        // 解析答案：answer_{questionId} = optionId
        Map<Integer, Integer> answerMap = new HashMap<>();
        for (Map.Entry<String, String> e : answers.entrySet()) {
            String key = e.getKey();
            if (key.startsWith("answer_")) {
                Integer qid = Integer.valueOf(key.substring("answer_".length()));
                Integer oid = Integer.valueOf(e.getValue());
                answerMap.put(qid, oid);
            }
        }

        try {
            int totalScore = studentExamService.submitExam(loginUser.getId(), examId, answerMap);
            SubmitExamResponse res = new SubmitExamResponse();
            res.setExamId(examId);
            res.setTotalScore(totalScore);
            return ResponseDTO.success(res);
        } catch (RepeatSubmitNotAllowedException ex) {
            return ResponseDTO.fail(ex.getMessage());
        } catch (Exception e) {
            return ResponseDTO.fail("提交失败：" + e.getMessage());
        }
    }

    // 内部类：接收答题返回结果
    @Setter
    @Getter
    public static class TakeExamResponse {
        private Integer examId;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer duration;
        private List<TakeExamQuestionView> questions;
    }

    // 内部类：提交考试返回结果
    @Setter
    @Getter
    public static class SubmitExamResponse {
        // Getter & Setter
        private Integer examId;
        private Integer totalScore;

    }
}