package com.playit.backend.model;

import jakarta.persistence.Entity;

@Entity
public abstract class Question extends Activite {
	private String explication;

	public Question() {
	}

	public Question(DifficulteActivite difficulte, String intitule, String explication) {
		super(difficulte, intitule);
		this.explication = explication;
	}

	public String getExplication() {
		return this.explication;
	}

	public void setExplication(String explication) {
		this.explication = explication;
	}

}