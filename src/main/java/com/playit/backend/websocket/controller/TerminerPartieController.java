package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class TerminerPartieController extends Controller {

	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		JsonElement idPartieObjet = data.get("idPartie");
		Long idPartie = idPartieObjet.getAsLong();

		Partie partie = null;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "reponseTerminerPartie");
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Partie non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		try {
			playITService.terminerPartie(partie);
		} catch (IllegalStateException e) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "reponseTerminerPartie");
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 422);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseTerminerPartie");
		response.addProperty("succes", true);

		String etatPartie = partie.getEtat()
		                          .toString();
		JsonObject dataObject = new JsonObject();
		dataObject.addProperty("etatPartie", etatPartie);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		response.addProperty("type", "notificationTerminerPartie");
		responseMessage = new TextMessage(response.toString());
		List<WebSocketSession> listeSocketSessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);

		for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
			sessionEquipe.sendMessage(responseMessage);
		}
	}

}
