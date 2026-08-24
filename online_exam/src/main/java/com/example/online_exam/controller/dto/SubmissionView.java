package com.example.online_exam.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class SubmissionView {
    private Long studentId;
    private String username;
    private LocalDateTime submitTime;
    private Integer totalScore;

}