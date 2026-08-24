package com.example.online_exam.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MyExamAnswerView {
    private Integer questionId;
    private String questionContent;
    private Integer score;

    private String myAnswer;      // student_answer.answer
    private Boolean isCorrect;    // student_answer.is_correct

    private String correctAnswer; // 从 option_item 里查到的正确选项内容

}