package com.playit.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.MiniJeu;
import com.playit.backend.metier.model.SoumissionMiniJeu;

public interface SoumissionMiniJeuRepository extends JpaRepository<SoumissionMiniJeu, Long> {

    Optional<SoumissionMiniJeu> findByMiniJeuAndEquipe(MiniJeu miniJeu, Equipe equipe);

    @Query(value = "SELECT SUM(s.score) as score FROM equipe e JOIN soumission_mini_jeu s ON e.id = s.equipe_id JOIN mini_jeu mj ON s.mini_jeu_id = mj.id JOIN plateau p ON mj.plateau_id = p.id WHERE e.id = ? AND p.id = ?", nativeQuery = true)
    Optional<Integer> findScoreByEquipeAndPlateau(Long id, Long id2);

}
