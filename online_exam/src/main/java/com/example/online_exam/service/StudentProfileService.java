package com.example.online_exam.service;

import com.example.online_exam.controller.dto.MyExamAnswerView;
import com.example.online_exam.controller.dto.MyExamSummaryView;
import com.example.online_exam.controller.dto.MyExamView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class StudentProfileService {

    private final JdbcTemplate jdbcTemplate;

    public StudentProfileService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 我的考试列表：只显示该学生实际有提交记录的考试（最新一次提交）
    public List<MyExamView> listMyExams(Long studentId) {
        String sql =
                "SELECT e.exam_id, e.exam_name, se.submit_time, se.total_score " +
                        "FROM exam e " +
                        "JOIN student_exam se ON se.student_id=? AND se.exam_id=e.exam_id " +
                        "  AND se.attempt_no = ( " +
                        "    SELECT COALESCE(MAX(se2.attempt_no),0) FROM student_exam se2 " +
                        "    WHERE se2.student_id=? AND se2.exam_id=e.exam_id " +
                        "  ) " +
                        "ORDER BY se.submit_time DESC, e.exam_id DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MyExamView v = new MyExamView();
            v.setExamId(rs.getInt("exam_id"));
            v.setExamName(rs.getString("exam_name"));

            Timestamp ts = rs.getTimestamp("submit_time");
            v.setSubmitTime(ts == null ? null : ts.toLocalDateTime());

            int score = rs.getInt("total_score");
            v.setTotalScore(rs.wasNull() ? null : score);
            return v;
        }, studentId, studentId);
    }

    // 我的考试详情：题目来自 question.exam_id；答案来自 student_answer
    public List<MyExamAnswerView> getMyExamDetail(Long studentId, Integer examId) {
        String sql =
                "SELECT q.question_id, q.content AS question_content, q.score, " +
                        "       sa.answer AS my_answer, sa.is_correct, " +
                        "       (SELECT oi.content FROM option_item oi " +
                        "           WHERE oi.question_id = q.question_id AND oi.is_correct = 1 " +
                        "           LIMIT 1) AS correct_answer " +
                        "FROM question q " +
                        "LEFT JOIN student_answer sa " +
                        "  ON sa.question_id = q.question_id AND sa.exam_id = q.exam_id AND sa.student_id = ? " +
                        "WHERE q.exam_id = ? " +
                        "ORDER BY q.question_id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MyExamAnswerView v = new MyExamAnswerView();
            v.setQuestionId(rs.getInt("question_id"));
            v.setQuestionContent(rs.getString("question_content"));
            v.setScore(rs.getInt("score"));
            v.setMyAnswer(rs.getString("my_answer"));

            Object ic = rs.getObject("is_correct");
            v.setIsCorrect(ic == null ? null : rs.getInt("is_correct") == 1);

            v.setCorrectAnswer(rs.getString("correct_answer"));
            return v;
        }, studentId, examId);
    }

    // 汇总：同样取最新一次提交
    public MyExamSummaryView getMyExamSummary(Long studentId, Integer examId) {
        String sql =
                "SELECT e.exam_id, e.exam_name, se.submit_time, se.total_score " +
                        "FROM exam e " +
                        "JOIN student_exam se ON se.student_id=? AND se.exam_id=e.exam_id " +
                        "  AND se.attempt_no = ( " +
                        "    SELECT COALESCE(MAX(se2.attempt_no),0) FROM student_exam se2 " +
                        "    WHERE se2.student_id=? AND se2.exam_id=e.exam_id " +
                        "  ) " +
                        "WHERE e.exam_id = ?";

        List<MyExamSummaryView> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            MyExamSummaryView v = new MyExamSummaryView();
            v.setExamId(rs.getInt("exam_id"));
            v.setExamName(rs.getString("exam_name"));

            Timestamp ts = rs.getTimestamp("submit_time");
            v.setSubmitTime(ts == null ? null : ts.toLocalDateTime());

            int score = rs.getInt("total_score");
            v.setTotalScore(rs.wasNull() ? null : score);
            return v;
        }, studentId, studentId, examId);

        return list.isEmpty() ? null : list.get(0);
    }
}