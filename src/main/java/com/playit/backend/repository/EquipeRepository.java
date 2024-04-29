package com.playit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.playit.backend.model.Equipe;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {

    public Optional<Equipe> findByNom(String nom);
}
