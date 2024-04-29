package com.playit.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;
import java.util.ArrayList;

@Entity
public class MaitreDuJeu {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String nom;
	private String motDePasse;

    @OneToMany(mappedBy = "maitreDuJeu")
    private List<Partie> listeParties = new ArrayList<>();

	public MaitreDuJeu() {
	}

	public MaitreDuJeu(String nom, String motDePasse) {
		this.nom = nom;
		this.motDePasse = motDePasse;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getNom() {
		return nom;
	}
	
	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getMotDePasse() {
		return motDePasse;
	}

	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}

	public List<Partie> getListeParties() {
		return listeParties;
	}

    public void addPartie(Partie partie){
        partie.setMaitreDuJeu(this);
        this.listeParties.add(partie);
    }

	public void setListeParties(List<Partie> listeParties) {
		this.listeParties = listeParties;
	}
	
}