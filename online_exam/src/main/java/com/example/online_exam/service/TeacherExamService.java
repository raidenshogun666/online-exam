package com.example.online_exam.service;

import com.example.online_exam.controller.dto.SubmissionView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.example.online_exam.controller.dto.TeacherSubmissionDetailView;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherExamService {

    private final JdbcTemplate jdbcTemplate;

    public TeacherExamService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SubmissionView> listSubmissions(Integer examId) {
        String sql =
                "SELECT se.student_id, u.username, se.submit_time, se.total_score " +
                        "FROM student_exam se " +
                        "JOIN users u ON se.student_id = u.id " +
                        "WHERE se.exam_id = ? " +
                        "ORDER BY se.total_score DESC, se.submit_time ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            SubmissionView v = new SubmissionView();
            v.setStudentId(rs.getLong("student_id"));
            v.setUsername(rs.getString("username"));

            Timestamp ts = rs.getTimestamp("submit_time");
            v.setSubmitTime(ts == null ? null : ts.toLocalDateTime());

            int score = rs.getInt("total_score");
            v.setTotalScore(rs.wasNull() ? null : score);

            return v;
        }, examId);
    }

    public List<TeacherSubmissionDetailView> getSubmissionDetail(Integer examId, Long studentId) {

        String sql =
                "SELECT q.question_id, q.content AS question_content, q.score, " +
                        "       sa.answer AS student_answer, sa.is_correct, " +
                        "       (SELECT oi.content FROM option_item oi " +
                        "           WHERE oi.question_id = q.question_id AND oi.is_correct = 1 " +
                        "           LIMIT 1) AS correct_answer " +
                        "FROM question q " +
                        "LEFT JOIN student_answer sa " +
                        "  ON sa.question_id = q.question_id AND sa.exam_id = q.exam_id AND sa.student_id = ? " +
                        "WHERE q.exam_id = ? " +
                        "ORDER BY q.question_id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TeacherSubmissionDetailView v = new TeacherSubmissionDetailView();
            v.setQuestionId(rs.getInt("question_id"));
            v.setQuestionContent(rs.getString("question_content"));
            v.setScore(rs.getInt("score"));
            v.setStudentAnswer(rs.getString("student_answer"));

            Object icObj = rs.getObject("is_correct");
            Boolean correct = (icObj == null) ? null : (rs.getInt("is_correct") == 1);
            v.setIsCorrect(correct);

            v.setCorrectAnswer(rs.getString("correct_answer"));

            int earned = (correct != null && correct) ? v.getScore() : 0;
            v.setEarnedScore(earned);

            return v;
        }, studentId, examId);
    }
}