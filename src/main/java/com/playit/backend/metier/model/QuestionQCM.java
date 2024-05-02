package com.playit.backend.metier.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

@Entity
public class QuestionQCM extends Question {
	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Proposition> listePropositions = new ArrayList<>();

	public QuestionQCM() {
	}

	public QuestionQCM(DifficulteActivite difficulteActivite, String intitule, int numeroActivite, String explication,
	    List<Proposition> propositions, Proposition bonneProposition) {
		super(difficulteActivite, intitule, numeroActivite, explication);
		this.listePropositions = propositions;
		this.bonneProposition = bonneProposition;
	}

	public List<Proposition> getListePropositions() {
		return this.listePropositions;
	}

	public void addProposition(Proposition proposition) {
		this.listePropositions.add(proposition);
	}

	public void setListePropositions(List<Proposition> propositions) {
		this.listePropositions = propositions;
	}
}