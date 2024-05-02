package com.playit.backend.metier.model;

import java.util.Arrays;
import java.util.List;

public enum EtatPartie {
	CREEE, ATTENTE_EQUIPE, EN_PAUSE, CHOIX_PLATEAU, ATTENTE_ACTIVITE, ACTIVITE_EN_COURS, EXPLICATION, TERMINEE;

	static {
		CREEE.precedents = Arrays.asList();
		ATTENTE_EQUIPE.precedents = Arrays.asList(CREEE, EN_PAUSE);
		EN_PAUSE.precedents = Arrays.asList(CHOIX_PLATEAU, EXPLICATION, ATTENTE_ACTIVITE);
		CHOIX_PLATEAU.precedents = Arrays.asList(ATTENTE_EQUIPE, EXPLICATION);
		ATTENTE_ACTIVITE.precedents = Arrays.asList(CHOIX_PLATEAU, EXPLICATION);
		ACTIVITE_EN_COURS.precedents = Arrays.asList(ATTENTE_ACTIVITE);
		EXPLICATION.precedents = Arrays.asList(ACTIVITE_EN_COURS);
		TERMINEE.precedents = Arrays.asList(CHOIX_PLATEAU, EXPLICATION, ATTENTE_ACTIVITE);
	}

	private List<EtatPartie> precedents;

	public boolean peutEtreSuivantDe(EtatPartie etat) {
		return this.precedents.contains(etat);
	}

}