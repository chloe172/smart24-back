package com.playit.backend.model;

import jakarta.persistence.OneToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;

@Entity
public class QuestionVraiFaux extends Question {
    @OneToOne(cascade = CascadeType.PERSIST)
	private Reponse bonneReponse;

    public QuestionVraiFaux() {
    }

    public QuestionVraiFaux(DifficulteActivite difficulteActivite, String intitule, String explication, Reponse bonneReponse) {
        super(difficulteActivite, intitule, explication);
        this.bonneReponse = bonneReponse;
    }
    
    public Reponse getBonneReponse() {
		return this.bonneReponse;
	}

	public void setBonneReponse(Reponse bonneReponse) {
		this.bonneReponse = bonneReponse;
	}

}