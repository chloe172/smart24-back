package com.playit.backend.metier.model;

public class ScorePlateau {
    private Plateau plateau;

    private Integer rang;

    private Integer score;

    private Equipe equipe;

    public ScorePlateau() {
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public void setPlateau(Plateau plateau) {
        this.plateau = plateau;
    }

    public Integer getRang() {
        return rang;
    }

    public String getCouleurBadge() {
        if (this.rang == 1) {
            return "or";
        } else if (this.rang == 2) {
            return "argent";
        } else if (this.rang == 3) {
            return "bronze";
        } else {
            return "blanc";
        }
    }

    public void setRang(Integer rang) {
        this.rang = rang;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

}
