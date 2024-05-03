package com.playit.backend.metier.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Question extends Activite {
	protected String explication;

	protected Duration temps = Duration.ofSeconds(30);

	protected int score;

	@OneToOne(cascade = CascadeType.PERSIST)
	protected Proposition bonneProposition;

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Proposition> listePropositions = new ArrayList<>();

	public Question() {
	}

	public Question(DifficulteActivite difficulteActivite, String intitule, int numeroActivite, String explication,
			List<Proposition> propositions, Proposition bonneProposition) {
		super(difficulteActivite, intitule, numeroActivite);
		this.explication = explication;
		this.score = difficulte.getPoints();
		this.listePropositions = propositions;
		this.bonneProposition = bonneProposition;
	}

	public String getExplication() {
		return this.explication;
	}

	public void setExplication(String explication) {
		this.explication = explication;
	}

	public Duration getTemps() {
		return this.temps;
	}

	public void setTemps(Duration temps) {
		this.temps = temps;
	}

	public int getScore() {
		return this.score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Proposition getBonneProposition() {
		return this.bonneProposition;
	}

	public void setBonneProposition(Proposition bonneProposition) {
		this.bonneProposition = bonneProposition;
	}

	public void addProposition(Proposition proposition) {
		this.listePropositions.add(proposition);
	}

	public List<Proposition> getListePropositions() {
		return this.listePropositions;
	}

}