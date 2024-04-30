package com.playit.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.playit.backend.model.Equipe;
import com.playit.backend.model.Partie;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {

	Optional<Equipe> findByNomAndPartie(@Param("nom") String nom, @Param("partie") Partie partie);
}
