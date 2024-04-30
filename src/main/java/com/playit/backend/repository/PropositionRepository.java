package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.model.Proposition;

public interface PropositionRepository extends JpaRepository<Proposition, Long> {

}
