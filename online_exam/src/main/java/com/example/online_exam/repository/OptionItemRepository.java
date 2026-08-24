package com.example.online_exam.repository;

import com.example.online_exam.entity.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionItemRepository extends JpaRepository<OptionItem, Integer> {
    List<OptionItem> findByQuestionId(Integer questionId);
}