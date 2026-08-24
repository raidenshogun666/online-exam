package com.example.online_exam.controller;

import com.example.online_exam.controller.dto.CreateExamDTO;
import com.example.online_exam.controller.dto.ExamDetailView;
import com.example.online_exam.controller.dto.ExamListView;
import com.example.online_exam.controller.dto.ResponseDTO;
import com.example.online_exam.controller.dto.UpdateExamRequest;
import com.example.online_exam.entity.Exam;
import com.example.online_exam.entity.User;
import com.example.online_exam.service.ExamService;
import com.example.online_exam.util.ExamTimeUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ExamController {
    private final JdbcTemplate jdbcTemplate;
    private final ExamService examService;

    public ExamController(JdbcTemplate jdbcTemplate, ExamService examService) {
        this.jdbcTemplate = jdbcTemplate;
        this.examService = examService;
    }

    // 考试列表
    @GetMapping("/exams")
    public ResponseDTO<List<ExamListView>> list(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();

        List<Exam> exams = examService.findAll();
        LocalDateTime now = LocalDateTime.now();

        List<ExamListView> res = exams.stream().map(e -> {
            ExamListView v = new ExamListView();
            v.setExamId(e.getExamId());
            v.setExamName(e.getExamName());
            v.setStartTime(e.getStartTime());
            v.setEndTime(e.getEndTime());
            v.setDuration(e.getDuration());
            v.setCreatorId(e.getCreatorId());
            v.setStatus(ExamTimeUtil.getStatus(e.getStartTime(), e.getEndTime(), now).name());
            return v;
        }).toList();

        return ResponseDTO.success(res);
    }

    // 创建考试（仅老师）
    @PostMapping("/exams")
    public ResponseDTO<Void> create(@RequestBody CreateExamDTO dto, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"teacher".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        try {

            int maxAttempts = getIntOrDefault(dto.getMaxAttempts(), 1);
            int allowLate = getBoolAs01(dto.getAllowLate(), false);
            int latePenaltyPercent = getIntOrDefault(dto.getLatePenaltyPercent(), 0);

            jdbcTemplate.update(
                    "INSERT INTO exam (exam_name, start_time, end_time, duration, creator_id, max_attempts, allow_late, late_penalty_percent) " +
                            "VALUES (?,?,?,?,?,?,?,?)",
                    dto.getExamName(),
                    LocalDateTime.parse(dto.getStartTime()),
                    LocalDateTime.parse(dto.getEndTime()),
                    dto.getDuration(),
                    loginUser.getId(),
                    maxAttempts,
                    allowLate,
                    latePenaltyPercent
            );
            return ResponseDTO.success(null);
        } catch (Exception e) {
            return ResponseDTO.fail("创建考试失败：" + e.getMessage());
        }
    }

    // 删除考试（仅老师）
    @DeleteMapping("/exams/{examId}")
    public ResponseDTO<Void> delete(@PathVariable Long examId, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"teacher".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        try {
            examService.deleteById(examId);
            return ResponseDTO.success(null);
        } catch (Exception e) {
            return ResponseDTO.fail("删除失败：" + e.getMessage());
        }
    }

    // 获取试卷详情（用于编辑页）
    @GetMapping("/exams/{examId}")
    public ResponseDTO<ExamDetailView> getExam(@PathVariable Integer examId, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();

        String sql = "SELECT exam_id, exam_name, start_time, end_time, duration, max_attempts, allow_late, late_penalty_percent " +
                "FROM exam WHERE exam_id = ?";
        List<ExamDetailView> list = jdbcTemplate.query(sql, (rs, i) -> {
            ExamDetailView v = new ExamDetailView();
            v.setExamId(rs.getInt("exam_id"));
            v.setExamName(rs.getString("exam_name"));
            v.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
            v.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
            v.setDuration(rs.getInt("duration"));

            v.setMaxAttempts(rs.getInt("max_attempts"));
            v.setAllowLate(rs.getInt("allow_late") == 1);
            v.setLatePenaltyPercent(rs.getInt("late_penalty_percent"));
            return v;
        }, examId);

        if (list.isEmpty()) return ResponseDTO.fail("试卷不存在");
        return ResponseDTO.success(list.get(0));
    }

    // 修改试卷信息（仅创建者老师）——更新新策略字段
    @PutMapping("/exams/{examId}")
    public ResponseDTO<Void> updateExam(@PathVariable Integer examId,
                                        @RequestBody UpdateExamRequest req,
                                        HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"teacher".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        Long creatorId = jdbcTemplate.queryForObject(
                "SELECT creator_id FROM exam WHERE exam_id = ?",
                Long.class, examId
        );
        if (creatorId == null) return ResponseDTO.fail("试卷不存在");
        if (!creatorId.equals(loginUser.getId())) return ResponseDTO.forbidden();

        int updated = jdbcTemplate.update(
                "UPDATE exam SET exam_name=?, start_time=?, end_time=?, duration=?, " +
                        "max_attempts=?, allow_late=?, late_penalty_percent=? " +
                        "WHERE exam_id=?",
                req.getExamName(),
                req.getStartTime(),
                req.getEndTime(),
                req.getDuration(),
                getIntOrDefault(req.getMaxAttempts(), 1),
                getBoolAs01(req.getAllowLate(), false),
                getIntOrDefault(req.getLatePenaltyPercent(), 0),
                examId
        );
        if (updated != 1) return ResponseDTO.fail("更新失败");
        return ResponseDTO.success(null);
    }

    private int getIntOrDefault(Integer v, int def) {
        return v == null ? def : v;
    }

    private int getBoolAs01(Boolean v, boolean def) {
        boolean b = (v == null ? def : v);
        return b ? 1 : 0;
    }
}