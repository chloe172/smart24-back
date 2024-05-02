package com.playit.backend.metier.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.playit.backend.metier.model.Activite;
import com.playit.backend.metier.model.ActiviteEnCours;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.EtatPartie;
import com.playit.backend.metier.model.MaitreDuJeu;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.model.Proposition;
import com.playit.backend.metier.model.Question;
import com.playit.backend.metier.model.Reponse;
import com.playit.backend.repository.ActiviteEnCoursRepository;
import com.playit.backend.repository.EquipeRepository;
import com.playit.backend.repository.MaitreDuJeuRepository;
import com.playit.backend.repository.PartieRepository;
import com.playit.backend.repository.PlateauRepository;
import com.playit.backend.repository.PropositionRepository;
import com.playit.backend.repository.ReponseRepository;

@Service
public class PlayITService {
	private static final int TAILLE_CODE_PIN = 6;
	private static final String CODE_PIN_CHARACTERES = "0123456789AZERTYUIOPQSDFGHJKLMWXCVBN";

	@Autowired
	private EquipeRepository equipeRepository;
	@Autowired
	private MaitreDuJeuRepository maitreDuJeuRepository;
	@Autowired
	private PartieRepository partieRepository;
	@Autowired
	private PlateauRepository plateauRepository;
	@Autowired
	private ReponseRepository reponseRepository;
	@Autowired
	private ActiviteEnCoursRepository activiteEnCoursRepository;
	@Autowired
	private PropositionRepository propositionRepository;

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

	public List<Plateau> listerPlateauxDansPartie(Partie partie) {
		return partie.getPlateaux();
	}

	public List<Plateau> listerPlateaux() {
		return this.plateauRepository.findAll();
	}

	public List<Partie> listerParties(MaitreDuJeu maitreDuJeu) {
		return this.partieRepository.findAllByMaitreDuJeu(maitreDuJeu);
	}

	public Partie creerPartie(String nom, MaitreDuJeu maitre, List<Plateau> listePlateaux) {
		Optional<Partie> result = this.partieRepository.findByNom(nom);
		if (result.isPresent()) {
			throw new IllegalStateException("Nom de partie déjà pris");
		}

		Partie partie = new Partie(nom);
		partie.setPlateaux(listePlateaux);
		partie.setMaitreDuJeu(maitre);
		String codePin = this.genererCodePin();
		partie.setCodePin(codePin);

		return this.partieRepository.saveAndFlush(partie);
	}

	public void attendreEquipes(Partie partie) {
		if (!EtatPartie.ATTENTE_EQUIPE_INSCRIPTION.peutEtreSuivantDe(partie.getEtat()) && !EtatPartie.ATTENTE_EQUIPE_RECONNEXION.peutEtreSuivantDe(partie.getEtat())) {
			throw new IllegalStateException("Impossible de passer en mode Attente Equipes");
		}
		if(partie.getEtat() == EtatPartie.CREEE) {
			partie.setEtat(EtatPartie.ATTENTE_EQUIPE_INSCRIPTION);
		} else {
			partie.setEtat(EtatPartie.ATTENTE_EQUIPE_RECONNEXION);
		}
		
		this.partieRepository.saveAndFlush(partie);
	}

	public void passerEnModeChoixPlateau(Partie partie) {
		if (!EtatPartie.CHOIX_PLATEAU.peutEtreSuivantDe(partie.getEtat())) {
			throw new IllegalStateException("Impossible de passer en mode Choix Plateau");
		}
		partie.setEtat(EtatPartie.CHOIX_PLATEAU);
		this.partieRepository.saveAndFlush(partie);
	}

	public void passerEnModeExplication(Partie partie) {
		if (!EtatPartie.EXPLICATION.peutEtreSuivantDe(partie.getEtat())) {
			throw new IllegalStateException("Impossible de passer en mode Explication");
		}
		partie.setEtat(EtatPartie.EXPLICATION);
		this.partieRepository.saveAndFlush(partie);
	}

	public List<Equipe> terminerExpliquation(Partie partie) {
		if (!EtatPartie.ATTENTE_ACTIVITE.peutEtreSuivantDe(partie.getEtat())) {
			throw new IllegalStateException("Impossible de terminer l'explication");
		}
		partie.setEtat(EtatPartie.ATTENTE_ACTIVITE);
		this.partieRepository.saveAndFlush(partie);

		return equipeRepository.findEquipeByPartieOrderByScoreDesc(partie);
	}

	@Transactional
	public void mettreEnPausePartie(Partie partie) {
		if (!EtatPartie.EN_PAUSE.peutEtreSuivantDe(partie.getEtat())) {
			throw new IllegalStateException("Impossible de mettre en pause");
		}
		partie.setEtat(EtatPartie.EN_PAUSE);
		for(Equipe equipe : partie.getEquipes()) {
			equipe.setEstConnecte(false);
			this.equipeRepository.save(equipe);
		}
		this.equipeRepository.flush();
		this.partieRepository.saveAndFlush(partie);
	}

	public void terminerPartie(Partie partie) {
		if (!EtatPartie.TERMINEE.peutEtreSuivantDe(partie.getEtat())) {
			throw new IllegalStateException("Impossible de terminer");
		}
		partie.setEtat(EtatPartie.TERMINEE);
		for(Equipe equipe : partie.getEquipes()) {
			equipe.setEstConnecte(false);
			this.equipeRepository.save(equipe);
		}
		this.equipeRepository.flush();
		this.partieRepository.saveAndFlush(partie);
	}

	public Partie validerCodePin(String codePin) {
		Partie partie = this.partieRepository.findByCodePin(codePin);
		if (partie == null) {
			throw new NotFoundException("Aucune partie avec ce code PIN");
		}
		return partie;
	}

	public Equipe inscrireEquipe(String nom, Partie partie) {
		if (partie.getEtat() != EtatPartie.ATTENTE_EQUIPE_INSCRIPTION) {
			throw new IllegalStateException("Impossible d'inscrire l'equipe");
		}
		Optional<Equipe> result = this.equipeRepository.findByNomAndPartie(nom, partie);
		if (result.isPresent()) {
			throw new IllegalStateException("Nom d'équipe déjà pris");
		}
		Equipe equipe = new Equipe();
		equipe.setNom(nom);
		equipe.setEstConnecte(true);
		equipe.setScore(0);
		equipe = this.equipeRepository.saveAndFlush(equipe);
		partie.addEquipe(equipe);
		this.partieRepository.saveAndFlush(partie);
		return equipe;
	}

	public Equipe modifierEquipe(Equipe equipe, String nouveauNom) {
		Partie partie = equipe.getPartie();
		Optional<Equipe> result = this.equipeRepository.findByNomAndPartie(nouveauNom, partie);
		if (partie.getEtat() != EtatPartie.CHOIX_PLATEAU && partie.getEtat() != EtatPartie.ATTENTE_ACTIVITE) {
			throw new IllegalStateException("Impossible de modifier l'equipe");
		}
		if (result.isPresent()) {
			throw new IllegalStateException("Nom d'équipe déjà pris");
		}
		if (nouveauNom != null && nouveauNom.length() > 0) {
			equipe.setNom(nouveauNom);
		}
		return this.equipeRepository.saveAndFlush(equipe);
	}

	public Equipe rejoindrePartieEquipe(Equipe equipe, Partie partie) {
		if (partie.getEtat() != EtatPartie.ATTENTE_EQUIPE_RECONNEXION) {
			throw new IllegalStateException("Impossible de reconnecter l'equipe");
		}
		Optional<Equipe> result = this.equipeRepository.findByIdAndPartie(equipe.getId(), partie);
		if (result.isEmpty()) {
			throw new IllegalStateException("Equipe non presente à la session precedente");
		}
		if (equipe.getEstConnecte() == true) {
			throw new IllegalStateException("Equipe deja connecte à la session");
		}
		equipe.setEstConnecte(true);
		this.equipeRepository.saveAndFlush(equipe);
		return equipe;
	}

	public ActiviteEnCours lancerActivite(Partie partie) {
		if (!EtatPartie.ACTIVITE_EN_COURS.peutEtreSuivantDe(partie.getEtat())) {
			throw new IllegalStateException("Impossible de passer en mode Activite");
		}
		partie.setEtat(EtatPartie.ACTIVITE_EN_COURS);

		Plateau plateauCourant = partie.getPlateauCourant();
		int indiceActiviteCourante = partie.getIndiceActivite();
		if (indiceActiviteCourante >= plateauCourant.getListeActivites()
		                                            .size()) {
			throw new IllegalStateException("Il ne reste aucune activité à réaliser dans ce plateau");
		}
		Activite activite = plateauCourant.getListeActivites()
		                                  .get(indiceActiviteCourante);
		partie.setIndiceActivite(indiceActiviteCourante + 1);

		ActiviteEnCours activiteEnCours = new ActiviteEnCours();
		activiteEnCours.setPartie(partie);
		activiteEnCours.setActivite(activite);

		this.activiteEnCoursRepository.saveAndFlush(activiteEnCours);
		this.partieRepository.saveAndFlush(partie);

		return activiteEnCours;
	}

	public int soumettreReponse(Partie partie, Equipe equipe, Proposition proposition,
	    ActiviteEnCours activiteEnCours) {
		if (partie.getEtat() != EtatPartie.ACTIVITE_EN_COURS) {
			throw new IllegalStateException("Impossible de soumettre une réponse");
		}

		Activite activite = activiteEnCours.getActivite();
		if (!(activite instanceof Question)) {
			throw new IllegalStateException("L'activité n'est pas une question !");
		}

		Question question = (Question) activite;
		Duration dureeQuestion = question.getTemps();
		LocalDateTime tempsLimite = activiteEnCours.getDate()
		                                           .plus(dureeQuestion);
		Reponse reponse = new Reponse();
		if (reponse.getDateSoumission()
		           .isAfter(tempsLimite)) {
			throw new IllegalStateException("La réponse a été soumise après le temps imparti.");
		}
		reponse.setEquipe(equipe);
		reponse.setProposition(proposition);
		activiteEnCours.addReponse(reponse);
		int score = reponse.calculerScoreEquipe();
		equipe.ajouterScore(score);

		this.reponseRepository.saveAndFlush(reponse);
		this.equipeRepository.saveAndFlush(equipe);

		return score;
	}

	public void choisirPlateau(Partie partie, Plateau plateau) {
		if (!partie.getPlateaux()
		           .stream()
		           .anyMatch(p -> p.getId()
		                           .equals(plateau.getId()))) {
			throw new IllegalArgumentException("Le plateau n'appartient pas à la partie");
		}

		if (partie.getEtat() != EtatPartie.CHOIX_PLATEAU) {
			throw new IllegalStateException("Impossible de sélectionner un plateau");
		}
		partie.setEtat(EtatPartie.ATTENTE_ACTIVITE);
		partie.setPlateauCourant(plateau);
		// TODO : ajouter un indice Acitivte pour chaque plateau - nouvelle entite
		// plateau en cours
		this.partieRepository.saveAndFlush(partie);
	}

	private Random random = new Random();

	private String genererCodePin() {
		int numberOfPossibleValues = CODE_PIN_CHARACTERES.length();

		StringBuilder codePin = new StringBuilder();
		for (int i = 0; i < TAILLE_CODE_PIN; i++) {
			int index = this.random.nextInt(numberOfPossibleValues);
			char randomChar = CODE_PIN_CHARACTERES.charAt(index);
			codePin.append(randomChar);
		}
		return codePin.toString();
	}

	public Partie trouverPartieParId(Long idPartie) {
		return this.partieRepository.findById(idPartie)
		                            .orElseThrow(() -> new NotFoundException(
		                                "La partie avec l'id " + idPartie + " n'existe pas"));
	}

	public MaitreDuJeu trouverMaitreDuJeuParId(Long idMaitreDuJeu) {
		return this.maitreDuJeuRepository.findById(idMaitreDuJeu)
		                                 .orElseThrow(() -> new NotFoundException(
		                                     "Le maitre du jeu avec l'id " + idMaitreDuJeu + " n'existe pas"));
	}

	public Plateau trouverPlateauParId(Long idPlateau) {
		return this.plateauRepository.findById(idPlateau)
		                             .orElseThrow(() -> new NotFoundException(
		                                 "Le plateau avec l'id " + idPlateau + " n'existe pas"));
	}

	public Equipe trouverEquipeParId(Long idEquipe) {
		return this.equipeRepository.findById(idEquipe)
		                            .orElseThrow(() -> new NotFoundException(
		                                "L'équipe avec l'id " + idEquipe + " n'existe pas"));
	}

	public Proposition trouverPropositionParId(Long idProposition) {
		return this.propositionRepository.findById(idProposition)
		                                 .orElseThrow(() -> new NotFoundException(
		                                     "La propositioni avec l'id " + idProposition + " n'existe pas"));
	}

	public ActiviteEnCours trouverActiviteEnCoursParId(Long idActiviteEnCours) {
		return this.activiteEnCoursRepository.findById(idActiviteEnCours)
		                                     .orElseThrow(() -> new NotFoundException("L'activité en cours avec l'id "
		                                         + idActiviteEnCours + " n'existe pas"));
	}

}
