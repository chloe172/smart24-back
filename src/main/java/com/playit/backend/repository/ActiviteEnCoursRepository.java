package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.metier.model.ActiviteEnCours;

public interface ActiviteEnCoursRepository extends JpaRepository<ActiviteEnCours, Long> {

}
