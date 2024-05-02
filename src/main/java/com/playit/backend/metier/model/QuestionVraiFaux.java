package com.playit.backend.metier.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

@Entity
public class QuestionVraiFaux extends Question {
	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Proposition> listePropositions = new ArrayList<>();

	public QuestionVraiFaux() {
	}

	public QuestionVraiFaux(DifficulteActivite difficulteActivite, String intitule, int numeroActivite,
	    String explication, boolean bonneProposition) {
		super(difficulteActivite, intitule, numeroActivite, explication);
		Proposition propositionVrai = new Proposition("Vrai");
		Proposition propositionFaux = new Proposition("Faux");
		this.listePropositions.add(propositionVrai);
		this.listePropositions.add(propositionFaux);
		this.bonneProposition = bonneProposition ? propositionVrai : propositionFaux;
	}

	public List<Proposition> getListePropositions() {
		return this.listePropositions;
	}

}