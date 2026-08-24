package com.example.online_exam.controller.dto;

import com.example.online_exam.entity.OptionItem;
import java.util.List;

public class QuestionView {
    private Integer questionId;
    private String content;
    private Integer score;
    private List<OptionItem> options;

    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public List<OptionItem> getOptions() { return options; }
    public void setOptions(List<OptionItem> options) { this.options = options; }
}