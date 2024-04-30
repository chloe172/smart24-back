package com.playit.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.model.MaitreDuJeu;

public interface MaitreDuJeuRepository extends JpaRepository<MaitreDuJeu, Long> {

	Optional<MaitreDuJeu> findByNom(String nom);
}