package com.playit.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	protected int numeroActivite;

	@Enumerated(EnumType.STRING)
	protected DifficulteActivite difficulte;
	protected String intitule;

	@ManyToOne
	protected Plateau plateau;

	public Activite() {
	}

	public Activite(DifficulteActivite difficulte, String intitule, int numeroActivite) {
		this.intitule = intitule;
		this.difficulte = difficulte;
		this.numeroActivite = numeroActivite;
	}

	public Long getId() {
		return this.id;
	}

	public DifficulteActivite getDifficulte() {
		return this.difficulte;
	}

	public void setDifficulte(DifficulteActivite difficulte) {
		this.difficulte = difficulte;
	}

	public String getIntitule() {
		return this.intitule;
	}

	public void setIntitule(String intitule) {
		this.intitule = intitule;
	}

	public Plateau getPlateau() {
		return this.plateau;
	}

	public void setPlateau(Plateau plateau) {
		this.plateau = plateau;
	}

	public int getNumeroActivite() {
		return this.numeroActivite;
	}

	public void setNumeroActivite(int numeroActivite) {
		this.numeroActivite = numeroActivite;
	}

}