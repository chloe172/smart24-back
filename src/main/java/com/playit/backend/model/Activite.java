package com.playit.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Activite {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected Long id;
	protected int difficulte;
	protected String intitule;

    @ManyToOne
    private Plateau plateau;

	public Activite() {
	}

	public Activite(int difficulte, String intitule) {
		this.intitule = intitule;
		this.difficulte = difficulte;
	}

	public Long getId(){
		return id;
	}

	public int getDifficulte() {
		return difficulte;
	} 

	public void setDifficulte(int difficulte) {
		this.difficulte = difficulte;
	}

	public String getIntitule() {
		return intitule;
	}
	
	public void setIntitule(String intitule) {
		this.intitule = intitule;
	}

    public void setPlateau(Plateau plateau) {
		this.plateau = plateau;
	}

}