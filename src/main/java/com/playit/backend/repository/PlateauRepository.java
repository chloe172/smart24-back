package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.model.Plateau;

public interface PlateauRepository extends JpaRepository<Plateau, Long> {
}
