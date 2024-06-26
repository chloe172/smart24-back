package com.playit.backend.metier.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;

@Entity
public class MaitreDuJeu {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String nom;
	private String motDePasseEncode;

	@OneToMany(mappedBy = "maitreDuJeu", fetch = FetchType.EAGER)
	private List<Partie> listeParties = new ArrayList<>();

	public MaitreDuJeu() {
	}

	public MaitreDuJeu(String nom, String motDePasse) {
		this.nom = nom;
		this.motDePasseEncode = motDePasse;
	}

	public Long getId() {
		return this.id;
	}

	public String getNom() {
		return this.nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getMotDePasseEncode() {
		return this.motDePasseEncode;
	}

	public void setMotDePasseEncode(String motDePasse) {
		this.motDePasseEncode = motDePasse;
	}

	public List<Partie> getListeParties() {
		return this.listeParties;
	}

	public void addPartie(Partie partie) {
		partie.setMaitreDuJeu(this);
		this.listeParties.add(partie);
	}

	public void setListeParties(List<Partie> listeParties) {
		this.listeParties = listeParties;
	}

}