package com.playit.backend.metier.model;

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
	
	private Boolean estConnecte;

	private int score;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
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

	public Boolean getEstConnecte() {
		return this.estConnecte;
	}

	public void setEstConnecte(Boolean estConnecte) {
		this.estConnecte = estConnecte;
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
		if (this.partie != null) {
			this.partie.removeEquipe(this);
		}
		this.partie = partie;
	}

	@Override
	public String toString() {
		return "Equipe {" + " id='" + this.getId() + "'" + ", nom='" + this.getNom() + "'" + ", score='"
		    + this.getScore() + "'" + ", partie='" + this.getPartie() + "'" + "}";
	}

	public void ajouterScore(int scoreEquipe) {
		this.score += scoreEquipe;
	}

}