package com.playit.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playit.backend.model.Equipe;
import com.playit.backend.model.EtatPartie;
import com.playit.backend.model.MaitreDuJeu;
import com.playit.backend.model.Partie;
import com.playit.backend.model.Plateau;
import com.playit.backend.repository.ActiviteRepository;
import com.playit.backend.repository.EquipeRepository;
import com.playit.backend.repository.MaitreDuJeuRepository;
import com.playit.backend.repository.MiniJeuRepository;
import com.playit.backend.repository.PartieRepository;
import com.playit.backend.repository.PlateauRepository;
import com.playit.backend.repository.QuestionRepository;

@Service
public class PlayITService {
	private static final int TAILLE_CODE_PIN = 6;

	private final ActiviteRepository activiteRepository;
	private final EquipeRepository equipeRepository;
	private final MaitreDuJeuRepository maitreDuJeuRepository;
	private final MiniJeuRepository miniJeuRepository;
	private final PartieRepository partieRepository;
	private final PlateauRepository plateauRepository;
	private final QuestionRepository questionRepository;

	public PlayITService(ActiviteRepository activiteRepository, EquipeRepository equipeRepository,
		MaitreDuJeuRepository maitreDuJeuRepository, MiniJeuRepository miniJeuRepository,
		PartieRepository partieRepository, PlateauRepository plateauRepository, QuestionRepository questionRepository) {
		this.activiteRepository = activiteRepository;
		this.equipeRepository = equipeRepository;
		this.maitreDuJeuRepository = maitreDuJeuRepository;
		this.miniJeuRepository = miniJeuRepository;
		this.plateauRepository = plateauRepository;
		this.partieRepository = partieRepository;
		this.questionRepository = questionRepository;
	}

	public MaitreDuJeu authentifier(String login, String mdp) {
		Optional<MaitreDuJeu> result = this.maitreDuJeuRepository.findByNom(login);
		if (result.isEmpty()) {
			throw new IllegalArgumentException("Compte Maître du Jeu non trouvé");
		}
		if (!result.get()
				   .getMotDePasse()
				   .equals(mdp)) {
			throw new IllegalArgumentException("Erreur de mot de passe");
		}
		return result.get();
	}

	public List<Plateau> listerPlateaux(Long idPartie) {
		Optional<Partie> result = this.partieRepository.findById(idPartie);
		if (result.isEmpty()) {
			throw new IllegalArgumentException("Partie non trouvée");
		}
		return result.get()
					 .getPlateaux();
	}

	public List<Plateau> listerPlateaux() {
		return this.plateauRepository.findAll();
	}

	public List<Equipe> listerEquipe(Long idPartie) {
		Optional<Partie> result = this.partieRepository.findById(idPartie);
		if (result.isEmpty()) {
			throw new IllegalArgumentException("Partie non trouvée");
		}
		return result.get()
					 .getEquipes();
	}

	@Transactional
	public Partie creerPartie(int nombreEquipes, MaitreDuJeu maitre, List<Plateau> listePlateaux) {
		Partie partie = new Partie();
		partie.setNombreEquipes(nombreEquipes);
		partie.setPlateaux(listePlateaux);
		partie.setMaitreDuJeu(maitre);
		String codePin = this.genererCodePin();
		partie.setCodePin(codePin);

		return this.partieRepository.saveAndFlush(partie);
	}

	@Transactional
	public void demarrerPartie(Partie partie) {
		partie.setEtat(EtatPartie.EN_COURS);
	}

	@Transactional
	public void mettreEnPausePartie(Partie partie) {
		partie.setEtat(EtatPartie.EN_PAUSE);
	}

	@Transactional
	public void terminerPartie(Partie partie) {
		partie.setEtat(EtatPartie.TERMINEE);
	}

	public List<Partie> listerParties() {
		return this.partieRepository.findAll();
	}

	public Partie validerCodePin(String codePin) {
		return this.partieRepository.findByCodePin(codePin);
	}

	@Transactional
	public Equipe inscrireEquipe(String nom, Partie partie) {
		Optional<Equipe> result = this.equipeRepository.findByNom(nom);
		if (result.isPresent()) {
			throw new IllegalStateException("nom d'équipe déjà pris");
		}
		Equipe equipe = new Equipe();
		equipe.setNom(nom);
		partie.addEquipe(equipe);
		equipe.setScore(0);
		return this.equipeRepository.saveAndFlush(equipe);
	}

	@Transactional
	public Equipe modifierEquipe(Long idEquipe, String nouveauNom) {
		Equipe equipe = this.equipeRepository.findById(idEquipe)
											 .orElseThrow(() -> new IllegalStateException(
												 "l'équipe avec l'id " + idEquipe + " n'existe pas"));
		Optional<Equipe> result = this.equipeRepository.findByNom(nouveauNom);
		if (result.isPresent()) {
			throw new IllegalStateException("nom d'équipe déjà pris");
		}
		if (nouveauNom != null && nouveauNom.length() > 0) {
			equipe.setNom(nouveauNom);
		}
		return equipe;
	}

	public void lancerActivite(Partie partie) {
		Plateau plateauCourant = partie.getPlateauCourant();
		if (plateauCourant.getActiviteCourante() == null) {
			plateauCourant.setActiviteCourante(plateauCourant.getListeActivites()
															 .get(0));
		} else {
			// todo aller chercher l'activité suivante
		}
	}

	public void envoyerReponse(Partie partie, Equipe equipe) {

	}

	private String genererCodePin() {
		String possibleValues = "0123456789AZERTYUIOPQSDFGHJKLMWXCVBN";
		StringBuilder codePin = new StringBuilder();
		for (int i = 0; i < TAILLE_CODE_PIN; i++) {
			codePin.append(possibleValues.charAt((int) (Math.random() * possibleValues.length())));
		}
		return codePin.toString();
	}

}