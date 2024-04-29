package com.playit.backend.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;

@Entity
public class QuestionQCM extends Question {
    @OneToMany(cascade = CascadeType.PERSIST)
	private List<Reponse> listeReponses = new ArrayList<>();

    @OneToOne
	private Reponse bonneReponse;

    public QuestionQCM() {
    }

    public QuestionQCM(DifficulteActivite difficulteActivite, String intitule, String explication, List<Reponse> reponses, Reponse bonneReponse) {
        super(difficulteActivite, intitule, explication);
        this.listeReponses = reponses;
        this.bonneReponse = bonneReponse;
    }

    public List<Reponse> getListeReponses() {
		return this.listeReponses;
	}

    public void addReponse(Reponse reponse){
        this.listeReponses.add(reponse);
    }

	public void setListeReponses(List<Reponse> reponses) {
		this.listeReponses = reponses;
	}
    
    public Reponse getBonneReponse() {
		return this.bonneReponse;
	}

	public void setBonneReponse(Reponse bonneReponse) {
		this.bonneReponse = bonneReponse;
	}
}