package com.example.online_exam.util;

import java.time.LocalDateTime;

public class ExamTimeUtil {

    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        ENDED
    }

    public static Status getStatus(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        if (startTime != null && now.isBefore(startTime)) return Status.NOT_STARTED;
        if (endTime != null && now.isAfter(endTime)) return Status.ENDED;
        return Status.IN_PROGRESS;
    }
}