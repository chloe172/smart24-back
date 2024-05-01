package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.model.Activite;
import com.playit.backend.model.ActiviteEnCours;
import com.playit.backend.model.Partie;
import com.playit.backend.model.Proposition;
import com.playit.backend.model.Question;
import com.playit.backend.model.QuestionQCM;
import com.playit.backend.model.QuestionVraiFaux;
import com.playit.backend.service.PlayITService;
import com.playit.backend.service.NotFoundException;
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
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		//Trouver prochaine activite
		ActiviteEnCours activiteEnCours;
		try {
			activiteEnCours = playITService.lancerActivite(partie);
		} catch (IllegalStateException e) {
			response.addProperty("type", "reponseLancerActivite");
			response.addProperty("messageErreur", e.getMessage());
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}
		
		Activite activite = activiteEnCours.getActivite();

		response.addProperty("type", "");
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
				for(WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
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
				for(WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
					sessionEquipe.sendMessage(responseMessage);
				}

				// Envoi du message au maitre du jeu
				response.addProperty("type", "reponseLancerActivite");
				response.add("data", dataObject);
				responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);
			}
			// TODO : lancer un timer sur la durée de la question pour envoyer la réponse après/mettre fin aux réponses
			// et passer la partie en état EXPLICATION
			/**
			 * A envoyer :
			 * - bonne réponse aux équipes et maitre du jeu
			 * - les explications au maitre du jeu
			 */
			Thread.sleep(10000);
			playITService.passerEnModeExplication(partie);
		} else {
			// mini jeu
		}

		return;

	}
	
}
