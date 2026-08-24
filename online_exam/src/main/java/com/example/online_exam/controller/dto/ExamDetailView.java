package com.example.online_exam.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ExamDetailView {
    private Integer examId;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;

    // 提交策略
    private Integer maxAttempts;
    private Boolean allowLate;
    private Integer latePenaltyPercent;
}