package com.playit.backend.model;

import java.util.List;

import jakarta.persistence.Entity;

@Entity
public class Question extends Activite {
	private String typeQuestion;
	private String intitule;
	private List<String> reponses;
	private String bonneReponse;
	private String explication;

	public Question() {
	}

	public Question(String intitule) {
		this.intitule = intitule;
	}

	/**
	 * @return the intitule
	 */
	public String getIntitule() {
		return this.intitule;
	}

}