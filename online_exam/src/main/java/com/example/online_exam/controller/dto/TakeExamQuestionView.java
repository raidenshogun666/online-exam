package com.example.online_exam.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class TakeExamQuestionView {
    private Integer questionId;
    private String content;
    private Integer score;
    private List<TakeExamOptionView> options;

}