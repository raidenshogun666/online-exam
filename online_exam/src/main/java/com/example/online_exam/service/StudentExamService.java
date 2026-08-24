package com.example.online_exam.service;

import com.example.online_exam.controller.dto.TakeExamOptionView;
import com.example.online_exam.controller.dto.TakeExamQuestionView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.RowMapper;
import com.example.online_exam.exception.RepeatSubmitNotAllowedException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@Service
public class StudentExamService {

    private final JdbcTemplate jdbcTemplate;

    public StudentExamService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<TakeExamQuestionView> getExamQuestionsForTaking(Integer examId) {
        String qsql =
                "SELECT q.question_id, q.content, q.score " +
                        "FROM question q " +
                        "WHERE q.exam_id = ? " +
                        "ORDER BY q.question_id";

        List<TakeExamQuestionView> questions = jdbcTemplate.query(qsql, (rs, rowNum) -> {
            TakeExamQuestionView v = new TakeExamQuestionView();
            v.setQuestionId(rs.getInt("question_id"));
            v.setContent(rs.getString("content"));
            v.setScore(rs.getInt("score"));
            return v;
        }, examId);

        String osql =
                "SELECT option_id, content " +
                        "FROM option_item " +
                        "WHERE question_id = ? " +
                        "ORDER BY option_id";

        for (TakeExamQuestionView q : questions) {
            List<TakeExamOptionView> ops = jdbcTemplate.query(osql, (rs, rowNum) -> {
                TakeExamOptionView o = new TakeExamOptionView();
                o.setOptionId(rs.getInt("option_id"));
                o.setContent(rs.getString("content"));
                return o;
            }, q.getQuestionId());
            q.setOptions(ops);
        }

        return questions;
    }

    @Transactional
    public int submitExam(Long studentId, Integer examId, Map<Integer, Integer> answersByQuestionId) {

        // 1) 读策略 + 时间
        var examRow = jdbcTemplate.queryForObject(
                "SELECT start_time, end_time, max_attempts, allow_late, late_penalty_percent " +
                        "FROM exam WHERE exam_id=?",
                (rs, rowNum) -> {
                    var m = new java.util.HashMap<String, Object>();
                    m.put("startTime", rs.getTimestamp("start_time").toLocalDateTime());
                    m.put("endTime", rs.getTimestamp("end_time").toLocalDateTime());
                    m.put("maxAttempts", rs.getInt("max_attempts"));
                    m.put("allowLate", rs.getInt("allow_late") == 1);
                    m.put("latePenaltyPercent", rs.getInt("late_penalty_percent"));
                    return m;
                },
                examId
        );

        LocalDateTime startTime = (LocalDateTime) examRow.get("startTime");
        LocalDateTime endTime = (LocalDateTime) examRow.get("endTime");
        int maxAttempts = (Integer) examRow.get("maxAttempts");
        boolean allowLate = (Boolean) examRow.get("allowLate");
        int latePenaltyPercent = (Integer) examRow.get("latePenaltyPercent");

        if (maxAttempts <= 0) maxAttempts = 1;
        if (latePenaltyPercent < 0) latePenaltyPercent = 0;
        if (latePenaltyPercent > 100) latePenaltyPercent = 100;

        // 2) 时间校验
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            throw new RuntimeException("考试未开始，无法提交");
        }
        boolean isLate = now.isAfter(endTime);
        if (isLate && !allowLate) {
            throw new RuntimeException("考试已结束，禁止迟到提交");
        }

        // 3) 次数校验：看已提交次数（行数）
        Integer submittedTimes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_exam WHERE student_id=? AND exam_id=?",
                Integer.class,
                studentId, examId
        );
        int used = submittedTimes == null ? 0 : submittedTimes;
        if (used >= maxAttempts) {
            throw new RepeatSubmitNotAllowedException("该考试最多提交 " + maxAttempts + " 次，你已达到上限。");
        }

        // 4) 本次 attempt_no
        Integer maxAttemptNo = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(attempt_no), 0) FROM student_exam WHERE student_id=? AND exam_id=?",
                Integer.class,
                studentId, examId
        );
        int attemptNo = (maxAttemptNo == null ? 0 : maxAttemptNo) + 1;

        // 5) 覆盖式答案
        jdbcTemplate.update("DELETE FROM student_answer WHERE student_id=? AND exam_id=?", studentId, examId);

        int totalScoreRaw = 0;

        for (Map.Entry<Integer, Integer> e : answersByQuestionId.entrySet()) {
            Integer questionId = e.getKey();
            Integer optionId = e.getValue();

            String answerContent = jdbcTemplate.queryForObject(
                    "SELECT content FROM option_item WHERE option_id=? AND question_id=?",
                    String.class,
                    optionId, questionId
            );

            Integer isCorrectInt = jdbcTemplate.queryForObject(
                    "SELECT is_correct FROM option_item WHERE option_id=? AND question_id=?",
                    Integer.class,
                    optionId, questionId
            );
            boolean correct = (isCorrectInt != null && isCorrectInt == 1);

            Integer score = jdbcTemplate.queryForObject(
                    "SELECT score FROM question WHERE question_id=?",
                    Integer.class,
                    questionId
            );
            if (correct) totalScoreRaw += (score == null ? 0 : score);

            jdbcTemplate.update(
                    "INSERT INTO student_answer(student_id, exam_id, question_id, answer, is_correct) VALUES (?,?,?,?,?)",
                    studentId, examId, questionId, answerContent, correct ? 1 : 0
            );
        }

        int totalScoreFinal = totalScoreRaw;
        if (isLate && latePenaltyPercent > 0) {
            double rate = (100.0 - latePenaltyPercent) / 100.0;
            totalScoreFinal = (int) Math.floor(totalScoreRaw * rate);
        }

        Timestamp submitTime = Timestamp.valueOf(now);

        // 6) 每次提交 INSERT 一行（带 attempt_no）
        jdbcTemplate.update(
                "INSERT INTO student_exam(student_id, exam_id, attempt_no, submit_time, total_score) VALUES (?,?,?,?,?)",
                studentId, examId, attemptNo, submitTime, totalScoreFinal
        );

        return totalScoreFinal;
    }


    public static class ExamTimeRange {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer duration;

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }


        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
    }

    public ExamTimeRange getExamTimeRange(Integer examId) {

        String sql = "SELECT start_time, end_time, duration FROM exam WHERE exam_id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            ExamTimeRange r = new ExamTimeRange();

            Timestamp st = rs.getTimestamp("start_time");
            Timestamp et = rs.getTimestamp("end_time");

            r.setStartTime(st == null ? null : st.toLocalDateTime());
            r.setEndTime(et == null ? null : et.toLocalDateTime());


            r.setDuration(rs.getInt("duration"));
            return r;
        }, examId);
    }
}