package com.playit.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.util.List;

@Entity
public class Partie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private int nombreEquipes;
	private String codePin;
	@Enumerated(EnumType.STRING)
	private EtatPartie etat = EtatPartie.EnAttente;

	@OneToOne
	private Plateau plateauCourant;

	@OneToMany
	private List<Plateau> listePlateaux;

	@OneToMany(mappedBy = "partie")
	private List<Equipe> listeEquipes;

	@ManyToOne
	private MaitreDuJeu maitreDuJeu;

	public Partie() {
	}

	public Long getId() {
		return id;
	}

	public String getCodePin() {
		return codePin;
	}

	public void setCodePin(String codePin) {
		this.codePin = codePin;
	}

	public Plateau getPlateauCourant() {
		return plateauCourant;
	}

	public void setPlateauCourant(Plateau plateauCourant) {
		this.plateauCourant = plateauCourant;
	}

	public List<Plateau> getPlateaux() {
		return listePlateaux;
	}

	public void setPlateaux(List<Plateau> plateaux) {
		this.listePlateaux = plateaux;
	}

	public List<Equipe> getEquipes() {
		return listeEquipes;
	}

	public void setEquipes(List<Equipe> equipes) {
		this.listeEquipes = equipes;
	}

	public MaitreDuJeu getMaitreDuJeu() {
		return maitreDuJeu;
	}

	public void setMaitreDuJeu(MaitreDuJeu maitreDuJeu) {
		this.maitreDuJeu = maitreDuJeu;
	}

	public int getNombreEquipes() {
		return nombreEquipes;
	}

	public void setNombreEquipes(int nombreEquipes) {
		this.nombreEquipes = nombreEquipes;
	}

	public EtatPartie getEtat() {
		return etat;
	}

	public void setEtat(EtatPartie etat) {
		this.etat = etat;
	}

	public void addEquipe(Equipe equipe){
		equipe.setPartie(this);
		this.listeEquipes.add(equipe);
	}

}
