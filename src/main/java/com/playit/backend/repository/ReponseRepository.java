package com.playit.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.playit.backend.metier.model.ActiviteEnCours;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Reponse;

public interface ReponseRepository extends JpaRepository<Reponse, Long> {

    Optional<Reponse> findByEquipeAndActiviteEnCours(Equipe equipe, ActiviteEnCours activiteEnCours);

    @Query(value = "SELECT SUM(r.score_equipe) as score FROM equipe e JOIN reponse r ON e.id = r.equipe_id JOIN activite_en_cours aec ON r.activite_en_cours_id = aec.id JOIN question q ON aec.activite_id = q.id JOIN plateau p ON q.plateau_id = p.id WHERE e.id = ? AND p.id = ?", nativeQuery = true)
    Integer findScoreByEquipeAndPlateau(Long equipeId, Long plateauId);

}
