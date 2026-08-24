
package com.example.online_exam.controller.dto;

import lombok.Data;

@Data
public class CreateExamDTO {
    private String examName;
    private String startTime;   // "2026-01-09T00:27:21" 这种
    private String endTime;
    private Integer duration;

    // 提交策略
    private Integer maxAttempts;        // 最大提交次数，默认 1
    private Boolean allowLate;          // 是否允许迟交，默认 false
    private Integer latePenaltyPercent; // 迟交扣分百分比 0~100，默认 0
}