package com.playit.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playit.backend.model.Activite;
import com.playit.backend.model.ActiviteEnCours;
import com.playit.backend.model.Equipe;
import com.playit.backend.model.EtatPartie;
import com.playit.backend.model.MaitreDuJeu;
import com.playit.backend.model.Partie;
import com.playit.backend.model.Plateau;
import com.playit.backend.model.Proposition;
import com.playit.backend.model.Question;
import com.playit.backend.model.Reponse;
import com.playit.backend.repository.ActiviteEnCoursRepository;
import com.playit.backend.repository.EquipeRepository;
import com.playit.backend.repository.MaitreDuJeuRepository;
import com.playit.backend.repository.PartieRepository;
import com.playit.backend.repository.PlateauRepository;
import com.playit.backend.repository.ReponseRepository;

@Service
@Transactional
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

	public List<Equipe> listerEquipe(Partie partie) {
		return partie.getEquipes();
	}

	public Partie creerPartie(String nom, MaitreDuJeu maitre, List<Plateau> listePlateaux) {
		Partie partie = new Partie(nom);
		partie.setPlateaux(listePlateaux);
		partie.setMaitreDuJeu(maitre);
		String codePin = this.genererCodePin();
		partie.setCodePin(codePin);

		return this.partieRepository.saveAndFlush(partie);
	}

	public void demarrerPartie(Partie partie) {
		partie.setEtat(EtatPartie.EN_COURS);
		this.partieRepository.saveAndFlush(partie);
	}

	public void mettreEnPausePartie(Partie partie) {
		partie.setEtat(EtatPartie.EN_PAUSE);
		this.partieRepository.saveAndFlush(partie);
	}

	public void terminerPartie(Partie partie) {
		partie.setEtat(EtatPartie.TERMINEE);
		this.partieRepository.saveAndFlush(partie);
	}

	public List<Partie> listerParties(MaitreDuJeu maitreDuJeu) {
		return this.partieRepository.findAllByMaitreDuJeu(maitreDuJeu);
	}

	public Partie validerCodePin(String codePin) {
		return this.partieRepository.findByCodePin(codePin);
	}

	public Equipe inscrireEquipe(String nom, Partie partie) {
		Optional<Equipe> result = this.equipeRepository.findByNomAndPartie(nom, partie);
		if (result.isPresent()) {
			throw new IllegalStateException("Nom d'équipe déjà pris");
		}
		Equipe equipe = new Equipe();
		equipe.setNom(nom);
		partie.addEquipe(equipe);
		equipe.setScore(0);
		return this.equipeRepository.saveAndFlush(equipe);
	}

	public Equipe modifierEquipe(Long idEquipe, String nouveauNom) {
		Equipe equipe = this.equipeRepository.findById(idEquipe)
				.orElseThrow(() -> new IllegalStateException(
						"l'équipe avec l'id " + idEquipe + " n'existe pas"));
		Optional<Equipe> result = this.equipeRepository.findByNomAndPartie(nouveauNom, equipe.getPartie());
		if (result.isPresent()) {
			throw new IllegalStateException("nom d'équipe déjà pris");
		}
		if (nouveauNom != null && nouveauNom.length() > 0) {
			equipe.setNom(nouveauNom);
		}
		return equipe;
	}

	public ActiviteEnCours lancerActivite(Partie partie) {
		Plateau plateauCourant = partie.getPlateauCourant();
		Activite activite;
		int indiceActiviteCourante = partie.getIndiceActivite();
		if (indiceActiviteCourante >= plateauCourant.getListeActivites()
				.size()) {
			throw new IllegalStateException("Plus d'activité à réaliser dans ce plateau");
		}
		activite = plateauCourant.getListeActivites()
				.get(indiceActiviteCourante);
		partie.setIndiceActivite(indiceActiviteCourante + 1);

		ActiviteEnCours activiteEnCours = new ActiviteEnCours();
		activiteEnCours.setPartie(partie);
		activiteEnCours.setActivite(activite);

		this.activiteEnCoursRepository.saveAndFlush(activiteEnCours);
		this.partieRepository.saveAndFlush(partie);

		return activiteEnCours;
	}

	public void soumettreReponse(Partie partie, Equipe equipe, Proposition proposition,
			ActiviteEnCours activiteEnCours) {
		Activite activite = activiteEnCours.getActivite();

		if (!(activite instanceof Question)) {
			throw new IllegalStateException("L'activité n'est pas une question !");
		}

		Reponse reponse = new Reponse();
		Duration dureeQuestion = ((Question) activite).getTemps();
		LocalDateTime tempsLimite = activiteEnCours.getDate()
				.plus(dureeQuestion);
		if (reponse.getDateSoumission()
				.isAfter(tempsLimite)) {
			throw new IllegalStateException("La réponse a été soumise après le temps imparti.");
		}
		reponse.setEquipe(equipe);
		reponse.setProposition(proposition);
		activiteEnCours.addReponse(reponse);
		reponse.calculerScoreEquipe();
		this.reponseRepository.saveAndFlush(reponse);
	}

	public void choisirPlateau(Partie partie, Plateau plateau) {
		// TODO : verifier que le plateau appartient bien aux plateaux de la partie
		partie.setPlateauCourant(plateau);
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
		// TODO : renvoyer une erreur si la partie n'existe pas
		return this.partieRepository.findById(idPartie).get();
	}

	public MaitreDuJeu trouverMaitreDuJeuParId(Long idMaitreDuJeu) {
		// TODO : renvoyer une erreur si le maitre du jeu n'existe pas
		return this.maitreDuJeuRepository.findById(idMaitreDuJeu).get();
	}

	public Plateau trouverPlateauParId(Long idPlateau) {
		// TODO : renvoyer une erreur si le maitre du jeu n'existe pas
		return this.plateauRepository.findById(idPlateau).get();
	}

}