package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.model.Activite;

public interface ActiviteRepository extends JpaRepository<Activite, Long> {

}
