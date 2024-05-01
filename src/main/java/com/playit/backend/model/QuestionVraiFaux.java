package com.playit.backend.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;

@Entity
public class QuestionVraiFaux extends Question {
	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private List<Proposition> listePropositions = new ArrayList<>();

    @OneToOne(cascade = CascadeType.PERSIST)
	private Proposition bonneProposition;

    public QuestionVraiFaux() {
    }

    public QuestionVraiFaux(DifficulteActivite difficulteActivite, String intitule, int numeroActivite, String explication, boolean bonneProposition) {
        super(difficulteActivite, intitule, numeroActivite, explication);
        Proposition propositionVrai = new Proposition("Vrai");
        Proposition propositionFaux = new Proposition("Faux");
        this.listePropositions.add(propositionVrai);
        this.listePropositions.add(propositionFaux);
        this.bonneProposition = bonneProposition ? propositionVrai : propositionFaux;
    }
    
    public Proposition getBonneProposition() {
		return this.bonneProposition;
	}

	public void setBonneProposition(Proposition bonneProposition) {
		this.bonneProposition = bonneProposition;
	}

    public List<Proposition> getListePropositions() {
        return this.listePropositions;
    }

}