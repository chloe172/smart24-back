package com.playit.backend.model;

import java.util.List;

import jakarta.persistence.Entity;

@Entity
public class Question extends Activite {
	private String typeQuestion;
	private List<String> reponses;
	private String bonneReponse;
	private String explication;

	public Question() {
	}

	public Question(String intitule) {
		this.intitule = intitule;
	}

	public String getTypeQuestion() {
		return this.typeQuestion;
	}

	public void setTypeQuestion(String typeQuestion) {
		this.typeQuestion = typeQuestion;
	}

	public String getIntitule() {
		return this.intitule;
	}

	public void setIntitule(String intitule) {
		this.intitule = intitule;
	}

	public List<String> getReponses() {
		return this.reponses;
	}

	public void setReponses(List<String> reponses) {
		this.reponses = reponses;
	}

	public String getBonneReponse() {
		return this.bonneReponse;
	}

	public void setBonneReponse(String bonneReponse) {
		this.bonneReponse = bonneReponse;
	}

	public String getExplication() {
		return this.explication;
	}

	public void setExplication(String explication) {
		this.explication = explication;
	}

}