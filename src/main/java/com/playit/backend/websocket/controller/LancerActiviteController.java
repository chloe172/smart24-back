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
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Proposition;
import com.playit.backend.metier.model.Question;
import com.playit.backend.metier.model.QuestionQCM;
import com.playit.backend.metier.model.QuestionVraiFaux;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class LancerActiviteController extends Controller {

	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		JsonElement idPartieObjet = data.get("idPartie");
		Long idPartie = idPartieObjet.getAsLong();

		JsonObject response = new JsonObject();
		JsonObject dataObject = new JsonObject();

		Partie partie;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("type", "reponseLancerActivite");
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
		} catch (IllegalStateException e) {
			response.addProperty("type", "reponseLancerActivite");
			response.addProperty("messageErreur", e.getMessage());
			response.addProperty("codeErreur", 422);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Activite activite = activiteEnCours.getActivite();

		response.addProperty("succes", true);

		List<WebSocketSession> listeSocketSessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);

		if (activite instanceof Question) {
			Question question = (Question) activite;
			JsonArray listePropositionsJson = new JsonArray();
			dataObject.addProperty("intitule", question.getIntitule());

			if (question instanceof QuestionQCM) {
				List<Proposition> listePropositions = ((QuestionQCM) activite).getListePropositions();
				for (Proposition proposition : listePropositions) {
					JsonObject propositionJson = new JsonObject();
					propositionJson.addProperty("intitule", proposition.getIntitule());
					propositionJson.addProperty("id", proposition.getId());
					listePropositionsJson.add(propositionJson);
				}
				dataObject.add("listePropositions", listePropositionsJson);
				response.add("data", dataObject);

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

			} else if (question instanceof QuestionVraiFaux) {
				List<Proposition> listePropositions = ((QuestionVraiFaux) activite).getListePropositions();
				for (Proposition proposition : listePropositions) {
					JsonObject propositionJson = new JsonObject();
					propositionJson.addProperty("id", proposition.getId());
					propositionJson.addProperty("intitule", proposition.getIntitule());
					listePropositionsJson.add(propositionJson);
				}
				dataObject.add("listePropositions", listePropositionsJson);
				response.add("data", dataObject);

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

			Thread finQuestionTimer = new Thread(() -> {
				Duration dureeQuestion = question.getTemps();
				Long dureeQuestionMillis = dureeQuestion.toMillis();
				try {
					Thread.sleep(dureeQuestionMillis);
				} catch (InterruptedException e) {}

				playITService.passerEnModeExplication(partie);

				Proposition bonneProposition = question.getBonneProposition();
				JsonObject bonnePropositionJson = new JsonObject();
				bonnePropositionJson.addProperty("intitule", bonneProposition.getIntitule());
				bonnePropositionJson.addProperty("id", bonneProposition.getId());
				
				JsonObject envoiBonneReponse = new JsonObject();
				envoiBonneReponse.add("data", bonnePropositionJson);
				envoiBonneReponse.addProperty("succes", true);

				// Envoi du message aux equipes
				envoiBonneReponse.addProperty("type", "notificationReponseActivite");
				TextMessage bonnePropositionMessage = new TextMessage(envoiBonneReponse.toString());
				for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
					try {
						sessionEquipe.sendMessage(bonnePropositionMessage);
					} catch (Exception e) {}
				}

				// Envoi au maitre du jeu
				bonnePropositionJson.addProperty("explication", question.getExplication());
				bonnePropositionJson.addProperty("intitule", bonneProposition.getIntitule());
				bonnePropositionJson.addProperty("id", bonneProposition.getId());
				envoiBonneReponse.add("data", bonnePropositionJson);
				bonnePropositionMessage = new TextMessage(envoiBonneReponse.toString());
				try {
					session.sendMessage(bonnePropositionMessage);
				} catch (Exception e) {}
				
			}, "finQuestionTimer");
			finQuestionTimer.start();

		} else {
			// mini jeu
		}

	}

}
