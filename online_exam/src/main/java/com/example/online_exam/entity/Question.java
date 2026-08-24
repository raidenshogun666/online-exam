package com.example.online_exam.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer questionId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 'single' / 'judge'
    @Column(name = "type", nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer score;

    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}