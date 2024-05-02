package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.metier.model.MiniJeu;

public interface MiniJeuRepository extends JpaRepository<MiniJeu, Long> {

}
