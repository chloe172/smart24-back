package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class InscrireEquipeController extends Controller {

	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.EQUIPE);

		Long idPartie = data.get("idPartie")
		                    .getAsLong();
		String nomEquipe = data.get("nomEquipe")
		                       .getAsString();

		Partie partie = null;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "reponseInscrireEquipe");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", "Partie non trouvée");
			response.addProperty("codeErreur", 404);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Equipe equipe = null;
		try {
			equipe = playITService.inscrireEquipe(nomEquipe, partie);
		} catch (Exception e) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "reponseInscrireEquipe");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", e.getMessage());
			response.addProperty("codeErreur", 422);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		session.getAttributes()
		       .put("idEquipe", equipe.getId());
		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseInscrireEquipe");
		response.addProperty("succes", true);

		JsonObject dataObject = new JsonObject();
		// TODO : mettre le json de la forme :
		/**
		 * {
		 *   partie: {
		 *     id,
		 *     nom, ...
		 *   },
		 *   equipe: {
		 *     id,
		 *     nom, ...
		 *   }
		 * }
		 */
		dataObject.addProperty("idEquipe", equipe.getId());
		dataObject.addProperty("nomEquipe", equipe.getNom());
		dataObject.addProperty("idPartie", partie.getId());
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		response.addProperty("type", "notificationInscrireEquipe");
		WebSocketSession sessionMaitreDuJeu = AssociationSessionsParties.getMaitreDuJeuPartie(partie);
		responseMessage = new TextMessage(response.toString());
		sessionMaitreDuJeu.sendMessage(responseMessage);

		List<WebSocketSession> sessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);
		for (WebSocketSession sessionEquipe : sessionsEquipes) {
			if(session != sessionEquipe) {
				sessionEquipe.sendMessage(responseMessage);
			}
		}

		return;
	}

}
