package com.playit.backend.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Partie;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {

	Optional<Equipe> findByNomAndPartie(@Param("nom") String nom, @Param("partie") Partie partie);

	Optional<Equipe> findByIdAndPartie(@Param("idEquipe") Long idEquipe, @Param("partie") Partie partie);

	List<Equipe> findAllByPartieOrderByScoreDesc(@Param("partie") Partie partie);

}
