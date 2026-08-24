package com.example.online_exam.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class MyExamView {
    private Integer examId;
    private String examName;
    private LocalDateTime submitTime;
    private Integer totalScore;

}