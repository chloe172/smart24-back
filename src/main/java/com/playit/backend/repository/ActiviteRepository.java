package com.playit.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.metier.model.Activite;

public interface ActiviteRepository extends JpaRepository<Activite, Long> {

	List<Activite> findAllByOrderByNumeroActiviteAsc();

}
