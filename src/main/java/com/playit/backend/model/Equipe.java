package com.playit.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Equipe {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nom;
	private int score;

	@ManyToOne(fetch = FetchType.EAGER, cascade= CascadeType.ALL)
	private Partie partie;

	public Equipe() {
	}

	public Long getId() {
		return this.id;
	}

	public String getNom() {
		return this.nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public int getScore() {
		return this.score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Partie getPartie() {
		return this.partie;
	}

	public void setPartie(Partie partie) {
		// TODO : supprimer l'équipe de l'ancienne partie s'il y en avait une
		this.partie = partie;
	}

	@Override
	public String toString() {
		return "Equipe {" + " id='" + getId() + "'" + ", nom='" + getNom() + "'" + ", score='" + getScore() + "'"
				+ ", partie='" + getPartie() + "'" + "}";
	}

    public void ajouterScore(int scoreEquipe) {
        this.score += scoreEquipe;
    }

}