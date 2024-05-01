package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.metier.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
