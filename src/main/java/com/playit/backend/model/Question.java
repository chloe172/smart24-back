package com.playit.backend.model;

import java.time.Duration;

import jakarta.persistence.Entity;

@Entity
public abstract class Question extends Activite {
	protected String explication;
	protected Duration temps = Duration.ofSeconds(30);
	protected int score;
	
	public Question() {
	}

	public Question(DifficulteActivite difficulte, String intitule, int numeroActivite, String explication) {
		super(difficulte, intitule, numeroActivite);
		this.explication = explication;
		score = difficulte.getPoints();
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

}