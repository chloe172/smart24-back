package com.playit.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.model.MaitreDuJeu;
import com.playit.backend.model.Partie;

public interface PartieRepository extends JpaRepository<Partie, Long> {

	Partie findByCodePin(String codePin);

	List<Partie> findAllByMaitreDuJeu(MaitreDuJeu maitreDuJeu);

    Optional<Partie> findByNom(String nom);

}
