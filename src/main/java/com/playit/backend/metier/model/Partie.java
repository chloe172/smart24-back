package com.playit.backend.metier.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class Partie {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String codePin;

	private String nom;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime date;

	@Enumerated(EnumType.STRING)
	private EtatPartie etat = EtatPartie.ATTENTE_EQUIPE_INSCRIPTION;

	@OneToOne(cascade = CascadeType.ALL)
	private PlateauEnCours plateauCourant;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "partie")
	private List<PlateauEnCours> listePlateauxEnCours = new ArrayList<>();

	@OneToMany(mappedBy = "partie", fetch = FetchType.EAGER)
	private List<Equipe> listeEquipes = new ArrayList<>();

	@ManyToOne
	private MaitreDuJeu maitreDuJeu;

	public Partie() {
	}

	public Partie(String nom) {
		this.nom = nom;
		this.date = LocalDateTime.now();
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

	public String getNom() {
		return this.nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public PlateauEnCours getPlateauCourant() {
		return this.plateauCourant;
	}

	public void setPlateauCourant(PlateauEnCours plateauCourant) {
		this.plateauCourant = plateauCourant;
	}

	public List<PlateauEnCours> getPlateauxEnCours() {
		return this.listePlateauxEnCours;
	}

	public List<Equipe> getEquipes() {
		return this.listeEquipes;
	}

	public void setEquipes(List<Equipe> equipes) {
		this.listeEquipes = equipes;
	}

	public void addEquipe(Equipe equipe) {
		equipe.setPartie(this);
		this.listeEquipes.add(equipe);
	}

	public void removeEquipe(Equipe equipe) {
		this.listeEquipes.remove(equipe);
	}

	public List<Equipe> getEquipesConnectees() {
		return this.listeEquipes.stream()
				.filter(Equipe::getEstConnecte)
				.toList();
	}

	public LocalDateTime getDate() {
		return this.date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public MaitreDuJeu getMaitreDuJeu() {
		return this.maitreDuJeu;
	}

	public void setMaitreDuJeu(MaitreDuJeu maitreDuJeu) {
		this.maitreDuJeu = maitreDuJeu;
	}

	public EtatPartie getEtat() {
		return this.etat;
	}

	public void setEtat(EtatPartie etat) {
		this.etat = etat;
	}

	public void setPlateaux(List<Plateau> listePlateaux) {
		for (Plateau plateau : listePlateaux) {
			this.listePlateauxEnCours.add(new PlateauEnCours(plateau, this));
		}
	}

}
