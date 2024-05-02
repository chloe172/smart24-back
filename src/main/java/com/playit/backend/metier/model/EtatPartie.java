package com.playit.backend.metier.model;

import java.util.Arrays;
import java.util.List;

public enum EtatPartie {
	ATTENTE_EQUIPE_INSCRIPTION, ATTENTE_EQUIPE_RECONNEXION, EN_PAUSE, CHOIX_PLATEAU, ATTENTE_ACTIVITE, ACTIVITE_EN_COURS, EXPLICATION, TERMINEE;

	static {
		ATTENTE_EQUIPE_RECONNEXION.precedents = Arrays.asList(EN_PAUSE);
		EN_PAUSE.precedents = Arrays.asList(CHOIX_PLATEAU, EXPLICATION, ATTENTE_ACTIVITE);
		CHOIX_PLATEAU.precedents = Arrays.asList(ATTENTE_EQUIPE_INSCRIPTION, ATTENTE_EQUIPE_RECONNEXION, EXPLICATION);
		ATTENTE_ACTIVITE.precedents = Arrays.asList(EXPLICATION);
		ACTIVITE_EN_COURS.precedents = Arrays.asList(ATTENTE_ACTIVITE, CHOIX_PLATEAU);
		EXPLICATION.precedents = Arrays.asList(ACTIVITE_EN_COURS);
		TERMINEE.precedents = Arrays.asList(CHOIX_PLATEAU, EXPLICATION, ATTENTE_ACTIVITE);
	}

	private List<EtatPartie> precedents;

	public boolean peutEtreSuivantDe(EtatPartie etat) {
		return this.precedents.contains(etat);
	}

}