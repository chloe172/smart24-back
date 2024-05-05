package com.playit.backend.metier.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class SoumissionMiniJeu {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private MiniJeu miniJeu;

    @ManyToOne
    private Equipe equipe;

    private int score;

    public SoumissionMiniJeu() {
    }

    public Long getId() {
        return id;
    }

    public MiniJeu getMiniJeu() {
        return miniJeu;
    }

    public void setMiniJeu(MiniJeu miniJeu) {
        this.miniJeu = miniJeu;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

}
