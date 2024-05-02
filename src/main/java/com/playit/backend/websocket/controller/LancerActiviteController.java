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
			if (question instanceof QuestionQCM) {
				List<Proposition> listePropositions = ((QuestionQCM) activite).getListePropositions();
				for (Proposition proposition : listePropositions) {
					JsonObject propositionJson = new JsonObject();
					propositionJson.addProperty("id", proposition.getId());
					propositionJson.addProperty("intitule", proposition.getIntitule());
					listePropositionsJson.add(propositionJson);
				}
				questionJson.add("listePropositions", listePropositionsJson);
				dataObject.add("question", questionJson);
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
				questionJson.add("listePropositions", listePropositionsJson);
				dataObject.add("question", questionJson);
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
				} catch (InterruptedException e) {
				}

				response.addProperty("type", "notificationReponseActivite");

				playITService.passerEnModeExplication(partie);

				Proposition bonneProposition = question.getBonneProposition();
				JsonObject bonnePropositionObject = new JsonObject();
				bonnePropositionObject.addProperty("id", bonneProposition.getId());
				bonnePropositionObject.addProperty("intitule", bonneProposition.getIntitule());
				questionJson.add("bonneProposition", bonnePropositionObject);

				// Envoi du message aux equipes : bonne proposition uniquement
				TextMessage bonnePropositionMessage = new TextMessage(response.toString());
				for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
					try {
						sessionEquipe.sendMessage(bonnePropositionMessage);
					} catch (Exception e) {
					}
				}

				// Envoi au maitre du jeu : bonne proposition et explication
				questionJson.addProperty("explication", question.getExplication());
				bonnePropositionMessage = new TextMessage(response.toString());
				try {
					session.sendMessage(bonnePropositionMessage);
				} catch (Exception e) {
				}

			}, "finQuestionTimer");
			finQuestionTimer.start();

		} else {
			// mini jeu
		}

	}

}
