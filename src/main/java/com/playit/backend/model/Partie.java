package com.playit.backend.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.util.List;
import java.util.ArrayList;

@Entity
public class Partie {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private int nombreEquipes;
	private String codePin;
	@Enumerated(EnumType.STRING)
	private EtatPartie etat = EtatPartie.EN_ATTENTE;

	@OneToOne
	private Plateau plateauCourant;

	@OneToMany
	private List<Plateau> listePlateaux = new ArrayList<>();

	@OneToMany(mappedBy = "partie")
	private List<Equipe> listeEquipes = new ArrayList<>();

	@ManyToOne
	private MaitreDuJeu maitreDuJeu;

	public Partie() {
	}

	public Long getId() {
		return this.id;
	}

	public String getCodePin() {
		return this.codePin;
	}

	public void setCodePin(String codePin) {
		this.codePin = codePin;
	}

	public Plateau getPlateauCourant() {
		return this.plateauCourant;
	}

	public void setPlateauCourant(Plateau plateauCourant) {
		this.plateauCourant = plateauCourant;
	}

	public List<Plateau> getPlateaux() {
		return this.listePlateaux;
	}

	public void setPlateaux(List<Plateau> plateaux) {
		this.listePlateaux = plateaux;
	}

	public List<Equipe> getEquipes() {
		return this.listeEquipes;
	}

	public void setEquipes(List<Equipe> equipes) {
		this.listeEquipes = equipes;
	}

	public MaitreDuJeu getMaitreDuJeu() {
		return this.maitreDuJeu;
	}

	public void setMaitreDuJeu(MaitreDuJeu maitreDuJeu) {
		this.maitreDuJeu = maitreDuJeu;
	}

	public int getNombreEquipes() {
		return this.nombreEquipes;
	}

	public void setNombreEquipes(int nombreEquipes) {
		this.nombreEquipes = nombreEquipes;
	}

	public EtatPartie getEtat() {
		return this.etat;
	}

	public void setEtat(EtatPartie etat) {
		this.etat = etat;
	}

	public void addEquipe(Equipe equipe) {
		equipe.setPartie(this);
		this.listeEquipes.add(equipe);
	}

}
