package com.example.online_exam.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ExamListView {
    private Integer examId;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Long creatorId;

    // NOT_STARTED / IN_PROGRESS / ENDED
    private String status;

}