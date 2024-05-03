package com.playit.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playit.backend.metier.model.ActiviteEnCours;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Reponse;

public interface ReponseRepository extends JpaRepository<Reponse, Long> {

    Optional<Reponse> findByEquipeAndActiviteEnCours(Equipe equipe, ActiviteEnCours activiteEnCours);

}
