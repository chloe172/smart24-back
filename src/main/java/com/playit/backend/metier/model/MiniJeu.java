package com.playit.backend.metier.model;

import jakarta.persistence.Entity;

@Entity
public class MiniJeu extends Activite {
    private String code;

    public MiniJeu() {
    }

    public MiniJeu(DifficulteActivite difficulte, String intitule, int numeroActivite, String nom, String code) {
        super(difficulte, intitule, numeroActivite);
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}