package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.metier.model.Reponse;

public interface ReponseRepository extends JpaRepository<Reponse, Long> {

}
