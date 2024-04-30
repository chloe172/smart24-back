package com.playit.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.model.Equipe;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {

	Optional<Equipe> findByNom(String nom);
}
