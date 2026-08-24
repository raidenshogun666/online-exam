package com.example.online_exam.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TeacherSubmissionDetailView {
    private Integer questionId;
    private String questionContent;
    private Integer score;

    private String studentAnswer;    // student_answer.answer
    private Boolean isCorrect;       // student_answer.is_correct

    private String correctAnswer;    // option_item.is_correct=1 的 content

    private Integer earnedScore;     // 正确=score，否则0；未作答=0

}