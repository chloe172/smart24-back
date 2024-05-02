package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.metier.model.PlateauEnCours;

public interface PlateauEnCoursRepository extends JpaRepository<PlateauEnCours, Long> {

}
