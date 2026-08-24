package com.example.online_exam.service;

import com.example.online_exam.entity.Exam;
import com.example.online_exam.repository.ExamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamService {

    private final ExamRepository examRepository;

    public ExamService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    public Exam save(Exam exam) {
        return examRepository.save(exam);
    }

    public void deleteById(Long examId) {
        examRepository.deleteById(Math.toIntExact(examId));
    }
}