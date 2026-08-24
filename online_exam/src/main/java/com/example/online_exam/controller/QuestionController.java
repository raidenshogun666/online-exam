package com.example.online_exam.controller;

import com.example.online_exam.controller.dto.QuestionView;
import com.example.online_exam.controller.dto.ResponseDTO;
import com.example.online_exam.entity.OptionItem;
import com.example.online_exam.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final JdbcTemplate jdbcTemplate;

    public QuestionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 获取考试题目列表（按 question.exam_id）
    @GetMapping("/exams/{examId}/questions")
    public ResponseDTO<List<QuestionView>> listQuestions(@PathVariable Integer examId, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();

        List<QuestionView> questions = jdbcTemplate.query(
                "SELECT question_id, content, score FROM question WHERE exam_id=? ORDER BY question_id ASC",
                (rs, i) -> {
                    QuestionView q = new QuestionView();
                    int qid = rs.getInt("question_id");
                    q.setQuestionId(qid);
                    q.setContent(rs.getString("content"));
                    q.setScore(rs.getInt("score"));

                    List<OptionItem> options = jdbcTemplate.query(
                            "SELECT option_id, question_id, content, is_correct FROM option_item WHERE question_id=? ORDER BY option_id ASC",
                            (rs2, j) -> {
                                OptionItem o = new OptionItem();
                                o.setOptionId(rs2.getInt("option_id"));
                                o.setQuestionId(rs2.getInt("question_id"));
                                o.setContent(rs2.getString("content"));
                                o.setIsCorrect(rs2.getInt("is_correct") == 1); // 关键：转成 Boolean
                                return o;
                            },
                            qid
                    );
                    q.setOptions(options);
                    return q;
                },
                examId
        );

        return ResponseDTO.success(questions);
    }

    // 添加考试题目（写入 question.exam_id）
    @PostMapping("/exams/{examId}/questions/add")
    public ResponseDTO<Void> addQuestion(@PathVariable Integer examId,
                                         @RequestBody AddQuestionRequest request,
                                         HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"teacher".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        Long creatorId = jdbcTemplate.queryForObject(
                "SELECT creator_id FROM exam WHERE exam_id=?",
                Long.class, examId
        );
        if (creatorId == null) return ResponseDTO.fail("试卷不存在");
        if (!creatorId.equals(loginUser.getId())) return ResponseDTO.forbidden();

        if (!validCorrectIndex(request.getCorrectIndex())) return ResponseDTO.fail("correctIndex 必须 A/B/C/D");

        // 1) insert question（写 exam_id）
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO question (exam_id, content, score, type) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, examId);
            ps.setString(2, request.getContent());
            ps.setInt(3, request.getScore() == null ? 0 : request.getScore());
            ps.setString(4, "single"); // ENUM('single','judge')
            return ps;
        }, kh);

        Number key = kh.getKey();
        if (key == null) return ResponseDTO.fail("创建题目失败");
        int questionId = key.intValue();

        // 2) insert options（先全 0）
        jdbcTemplate.update("INSERT INTO option_item (question_id, content, is_correct) VALUES (?,?,0)", questionId, request.getA());
        jdbcTemplate.update("INSERT INTO option_item (question_id, content, is_correct) VALUES (?,?,0)", questionId, request.getB());
        jdbcTemplate.update("INSERT INTO option_item (question_id, content, is_correct) VALUES (?,?,0)", questionId, request.getC());
        jdbcTemplate.update("INSERT INTO option_item (question_id, content, is_correct) VALUES (?,?,0)", questionId, request.getD());

        // 3) set correct
        List<Integer> optionIds = jdbcTemplate.query(
                "SELECT option_id FROM option_item WHERE question_id=? ORDER BY option_id ASC",
                (rs, i) -> rs.getInt("option_id"),
                questionId
        );
        jdbcTemplate.update("UPDATE option_item SET is_correct=1 WHERE option_id=?",
                optionIds.get(correctPos(request.getCorrectIndex()))
        );

        return ResponseDTO.success(null);
    }

    // 修改题目
    @PutMapping("/exams/{examId}/questions/{questionId}")
    public ResponseDTO<Void> updateQuestion(@PathVariable Integer examId,
                                            @PathVariable Integer questionId,
                                            @RequestBody AddQuestionRequest request,
                                            HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"teacher".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        Long creatorId = jdbcTemplate.queryForObject(
                "SELECT creator_id FROM exam WHERE exam_id=?",
                Long.class, examId
        );
        if (creatorId == null) return ResponseDTO.fail("试卷不存在");
        if (!creatorId.equals(loginUser.getId())) return ResponseDTO.forbidden();

        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM question WHERE question_id=? AND exam_id=?",
                Integer.class, questionId, examId
        );
        if (cnt == null || cnt == 0) return ResponseDTO.fail("题目不存在或不属于该试卷");

        if (!validCorrectIndex(request.getCorrectIndex())) return ResponseDTO.fail("correctIndex 必须 A/B/C/D");

        jdbcTemplate.update(
                "UPDATE question SET content=?, score=? WHERE question_id=?",
                request.getContent(),
                request.getScore() == null ? 0 : request.getScore(),
                questionId
        );

        List<Integer> optionIds = jdbcTemplate.query(
                "SELECT option_id FROM option_item WHERE question_id=? ORDER BY option_id ASC",
                (rs, i) -> rs.getInt("option_id"),
                questionId
        );
        if (optionIds.size() < 4) return ResponseDTO.fail("该题选项不足4个");

        jdbcTemplate.update("UPDATE option_item SET content=? WHERE option_id=?", request.getA(), optionIds.get(0));
        jdbcTemplate.update("UPDATE option_item SET content=? WHERE option_id=?", request.getB(), optionIds.get(1));
        jdbcTemplate.update("UPDATE option_item SET content=? WHERE option_id=?", request.getC(), optionIds.get(2));
        jdbcTemplate.update("UPDATE option_item SET content=? WHERE option_id=?", request.getD(), optionIds.get(3));

        jdbcTemplate.update("UPDATE option_item SET is_correct=0 WHERE question_id=?", questionId);
        jdbcTemplate.update("UPDATE option_item SET is_correct=1 WHERE option_id=?",
                optionIds.get(correctPos(request.getCorrectIndex()))
        );

        return ResponseDTO.success(null);
    }

    // 删除题目
    @DeleteMapping("/exams/{examId}/questions/{questionId}")
    public ResponseDTO<Void> deleteQuestion(@PathVariable Integer examId,
                                            @PathVariable Integer questionId,
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

        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM question WHERE question_id=? AND exam_id=?",
                Integer.class, questionId, examId
        );
        if (cnt == null || cnt == 0) return ResponseDTO.fail("题目不存在或不属于该试卷");

        jdbcTemplate.update("DELETE FROM question WHERE question_id=?", questionId);
        return ResponseDTO.success(null);
    }

    @Setter
    @Getter
    public static class AddQuestionRequest {
        private String content;
        private Integer score;
        private String a;
        private String b;
        private String c;
        private String d;
        private String correctIndex;
    }

    private boolean validCorrectIndex(String s) {
        if (s == null) return false;
        String u = s.toUpperCase();
        return u.equals("A") || u.equals("B") || u.equals("C") || u.equals("D");
    }

    private int correctPos(String s) {
        return switch (s.toUpperCase()) {
            case "A" -> 0;
            case "B" -> 1;
            case "C" -> 2;
            case "D" -> 3;
            default -> 0;
        };
    }
}