package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.metier.model.Plateau;

public interface PlateauRepository extends JpaRepository<Plateau, Long> {

	Plateau findByNom(String string);

}
