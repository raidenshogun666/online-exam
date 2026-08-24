package com.example.online_exam.service;

import com.example.online_exam.controller.dto.QuestionView;
import com.example.online_exam.entity.OptionItem;
import com.example.online_exam.entity.Question;
import com.example.online_exam.repository.OptionItemRepository;
import com.example.online_exam.repository.QuestionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final OptionItemRepository optionItemRepository;
    private final JdbcTemplate jdbcTemplate;

    public QuestionService(QuestionRepository questionRepository,
                           OptionItemRepository optionItemRepository,
                           JdbcTemplate jdbcTemplate) {
        this.questionRepository = questionRepository;
        this.optionItemRepository = optionItemRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Integer addSingleChoiceQuestionToExam(Integer examId,
                                                 String content,
                                                 Integer score,
                                                 String a, String b, String c, String d,
                                                 String correctIndex) {

        // 1) 插入 question
        Question q = new Question();
        q.setContent(content);
        q.setType("single");
        q.setScore(score);
        Question saved = questionRepository.save(q);

        Integer qid = saved.getQuestionId();

        // 2) 插入 option_item（4行）
        optionItemRepository.save(buildOption(qid, a, "A".equalsIgnoreCase(correctIndex)));
        optionItemRepository.save(buildOption(qid, b, "B".equalsIgnoreCase(correctIndex)));
        optionItemRepository.save(buildOption(qid, c, "C".equalsIgnoreCase(correctIndex)));
        optionItemRepository.save(buildOption(qid, d, "D".equalsIgnoreCase(correctIndex)));

        // 3) 绑定 exam_question
        jdbcTemplate.update(
                "INSERT INTO exam_question(exam_id, question_id) VALUES (?, ?)",
                examId, qid
        );

        return qid;
    }

    private OptionItem buildOption(Integer qid, String content, boolean correct) {
        OptionItem o = new OptionItem();
        o.setQuestionId(qid);
        o.setContent(content);
        o.setIsCorrect(correct);
        return o;
    }

    public List<QuestionView> listQuestionsByExamId(Integer examId) {


        String sql =
                "SELECT q.question_id, q.content, q.score " +
                        "FROM exam_question eq " +
                        "JOIN question q ON eq.question_id = q.question_id " +
                        "WHERE eq.exam_id = ? " +
                        "ORDER BY q.question_id";

        RowMapper<QuestionView> mapper = (rs, rowNum) -> {
            QuestionView v = new QuestionView();
            v.setQuestionId(rs.getInt("question_id"));
            v.setContent(rs.getString("content"));
            v.setScore(rs.getInt("score"));
            return v;
        };

        List<QuestionView> list = jdbcTemplate.query(sql, mapper, examId);

        // 再查每道题的选项
        for (QuestionView v : list) {
            List<OptionItem> options = optionItemRepository.findByQuestionId(v.getQuestionId());
            v.setOptions(options);
        }

        return list;
    }
}