package com.playit.backend.metier.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class Partie {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String codePin;

	@Column(unique = true)
	private String nom;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime date;

	@Enumerated(EnumType.STRING)
	private EtatPartie etat = EtatPartie.ATTENTE_EQUIPE_INSCRIPTION;

	@ManyToOne
	private Plateau plateauCourant;

	private int indiceActivite;

	@ManyToMany(fetch = FetchType.EAGER)
	private List<Plateau> listePlateaux = new ArrayList<>();

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

	public int getIndiceActivite() {
		return this.indiceActivite;
	}

	public void setIndiceActivite(int indiceActivite) {
		this.indiceActivite = indiceActivite;
	}

	public String getNom() {
		return this.nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
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

	public void addEquipe(Equipe equipe) {
		equipe.setPartie(this);
		this.listeEquipes.add(equipe);
	}

	public void removeEquipe(Equipe equipe) {
		this.listeEquipes.remove(equipe);
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

	public Activite getActiviteCourante() {
		return this.plateauCourant.getListeActivites()
		                          .get(this.indiceActivite);
	}

}
