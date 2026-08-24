package com.example.online_exam.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateQuestionRequest {
    private String content;
    private Integer score;

    private String a;
    private String b;
    private String c;
    private String d;

    // "A"|"B"|"C"|"D"
    private String correctIndex;
}