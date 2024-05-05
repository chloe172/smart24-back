package com.playit.backend.websocket.controller;

import java.util.List;
import java.time.Duration;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Activite;
import com.playit.backend.metier.model.ActiviteEnCours;
import com.playit.backend.metier.model.MiniJeu;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Proposition;
import com.playit.backend.metier.model.Question;
import com.playit.backend.metier.model.ScorePlateau;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.PartieThreadAttente;
import com.playit.backend.websocket.handler.SessionRole;

public class LancerActiviteController extends Controller {

	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		JsonElement idPartieObjet = data.get("idPartie");
		Long idPartie = idPartieObjet.getAsLong();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseLancerActivite");
		response.addProperty("succes", true);

		Partie partie;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", "Partie non trouvée");
			response.addProperty("codeErreur", 404);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		// Trouver prochaine activite
		ActiviteEnCours activiteEnCours;
		try {
			activiteEnCours = playITService.lancerActivite(partie);
			// TODO : vérifier qu'il y a un plateau sélectionné
		} catch (IllegalStateException e) {
			response.addProperty("messageErreur", e.getMessage());
			response.addProperty("codeErreur", 422);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		List<WebSocketSession> listeSocketSessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);
		JsonObject dataObject = new JsonObject();

		Activite activite = activiteEnCours.getActivite();
		if (activite instanceof Question) {
			Question question = (Question) activite;
			JsonObject questionJson = new JsonObject();
			questionJson.addProperty("id", question.getId());
			questionJson.addProperty("intitule", question.getIntitule());
			questionJson.addProperty("temps", question.getTemps()
					.toSeconds());

			JsonArray listePropositionsJson = new JsonArray();
			List<Proposition> listePropositions = question.getListePropositions();

			for (Proposition proposition : listePropositions) {
				JsonObject propositionJson = new JsonObject();
				propositionJson.addProperty("id", proposition.getId());
				propositionJson.addProperty("intitule", proposition.getIntitule());
				listePropositionsJson.add(propositionJson);
			}
			questionJson.add("listePropositions", listePropositionsJson);
			dataObject.add("question", questionJson);
			dataObject.addProperty("idActiviteEnCours", activiteEnCours.getId());
			dataObject.addProperty("nomPlateauCourant", partie.getPlateauCourant().getPlateau().getNom());
			dataObject.addProperty("typeActivite", "question");
			response.add("data", dataObject);

			Thread finQuestionTimer = new Thread(() -> {
				Duration dureeQuestion = question.getTemps();
				Long dureeQuestionMillis = dureeQuestion.toMillis();
				try {
					Thread.sleep(dureeQuestionMillis);
				} catch (InterruptedException e) {
					return;
				}

				response.addProperty("type", "notificationReponseActivite");

				playITService.passerEnModeExplication(partie);
				Partie finPartie = playITService.trouverPartieParId(idPartie);
				Plateau plateau = partie.getPlateauCourant().getPlateau();
				List<ScorePlateau> listeScore = playITService.obtenirEquipesParRang(partie, plateau);

				Proposition bonneProposition = question.getBonneProposition();
				JsonObject bonnePropositionObject = new JsonObject();
				bonnePropositionObject.addProperty("id", bonneProposition.getId());
				bonnePropositionObject.addProperty("intitule", bonneProposition.getIntitule());
				questionJson.add("bonneProposition", bonnePropositionObject);

				// Envoi du message aux equipes : bonne proposition uniquement
				questionJson.add("bonneProposition", bonnePropositionObject);
				dataObject.add("question", questionJson);
				response.add("data", dataObject);

				for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
					Long idEquipe = (Long) sessionEquipe.getAttributes().get("idEquipe");
					Equipe equipe = null;
					try {
						equipe = playITService.trouverEquipeParId(idEquipe);
					} catch (NotFoundException e) {
						continue;
					}
					JsonObject equipeJson = new JsonObject();
					equipeJson.addProperty("id", equipe.getId());
					equipeJson.addProperty("nom", equipe.getNom());
					final Equipe finalEquipe = equipe; // Pour fix un problème de portée askip
					int score = listeScore.stream()
							.filter(scorePlateau -> scorePlateau.getEquipe().getId().equals(finalEquipe.getId()))
							.findFirst()
							.map(ScorePlateau::getScore)
							.orElse(0);
					equipeJson.addProperty("score", score);
					dataObject.add("equipe", equipeJson);
					TextMessage bonnePropositionMessage = new TextMessage(response.toString());
					try {
						sessionEquipe.sendMessage(bonnePropositionMessage);
					} catch (Exception e) {
					}
				}

				// Envoi au maitre du jeu : bonne proposition et explication
				questionJson.addProperty("explication", question.getExplication());
				JsonArray listeEquipesJson = new JsonArray();
				List<Equipe> listeEquipes = playITService.obtenirEquipesParRang(finPartie);
				for (int i = 0; i < listeEquipes.size(); i++) {
					Equipe equipe = listeEquipes.get(i);
					JsonObject equipeJson = new JsonObject();
					equipeJson.addProperty("id", equipe.getId());
					equipeJson.addProperty("nom", equipe.getNom());
					int score = listeScore.stream()
							.filter(scorePlateau -> scorePlateau.getEquipe().getId().equals(equipe.getId()))
							.findFirst()
							.map(ScorePlateau::getScore)
							.orElse(0);
					equipeJson.addProperty("score", score);
					equipeJson.addProperty("avatar", equipe.getAvatar().toString());
					if (i == 0) {
						equipeJson.addProperty("rang", "1er");
					} else {
						equipeJson.addProperty("rang", i + 1 + "ème");
					}

					listeEquipesJson.add(equipeJson);
				}
				dataObject.add("listeEquipes", listeEquipesJson);
				response.add("data", dataObject);

				TextMessage bonnePropositionMessage = new TextMessage(response.toString());
				try {
					session.sendMessage(bonnePropositionMessage);
				} catch (Exception e) {
				}

			}, "finQuestionTimer");
			PartieThreadAttente.addThread(partie, finQuestionTimer);
			finQuestionTimer.start();

		} else { // mini jeu
			MiniJeu miniJeu = (MiniJeu) activite;

			JsonObject miniJeuJson = new JsonObject();
			miniJeuJson.addProperty("id", miniJeu.getId());
			miniJeuJson.addProperty("intitule", miniJeu.getIntitule());
			miniJeuJson.addProperty("code", miniJeu.getCode());
			dataObject.add("minijeu", miniJeuJson);
			dataObject.addProperty("idActiviteEnCours", activiteEnCours.getId());
			dataObject.addProperty("typeActivite", "minijeu");
			response.add("data", dataObject);
		}

		// Envoi du message aux equipes
		response.addProperty("type", "notificationLancerActivite");
		TextMessage responseMessage = new TextMessage(response.toString());
		for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
			sessionEquipe.sendMessage(responseMessage);
		}

		// Envoi du message au maitre du jeu
		response.addProperty("type", "reponseLancerActivite");
		response.add("data", dataObject);
		responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

	}

}
