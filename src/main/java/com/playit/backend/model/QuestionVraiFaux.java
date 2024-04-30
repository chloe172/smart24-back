package com.playit.backend.model;

import jakarta.persistence.OneToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;

@Entity
public class QuestionVraiFaux extends Question {
    @OneToOne(cascade = CascadeType.PERSIST)
	private Proposition bonneProposition;

    public QuestionVraiFaux() {
    }

    public QuestionVraiFaux(DifficulteActivite difficulteActivite, String intitule, int numeroActivite, String explication, Proposition bonneProposition) {
        super(difficulteActivite, intitule, numeroActivite, explication);
        this.bonneProposition = bonneProposition;
    }
    
    public Proposition getBonneProposition() {
		return this.bonneProposition;
	}

	public void setBonneProposition(Proposition bonneProposition) {
		this.bonneProposition = bonneProposition;
	}

}