package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.model.Partie;
import com.playit.backend.service.PlayITService;
import com.playit.backend.service.NotFoundException;
import com.playit.backend.websocket.handler.SessionRole;

public class MettreEnPauseController extends Controller {

	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		JsonObject response = new JsonObject();
		JsonObject dataObject = new JsonObject();

		JsonElement idPartieObjet = data.get("idPartie");
		Long idPartie = idPartieObjet.getAsLong();

		Partie partie = null;
		
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("type", "reponseMettreEnPausePartie");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", "Partie non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		try {
			playITService.mettreEnPausePartie(partie);
		} catch (IllegalStateException e) {
			response.addProperty("type", "reponseMettreEnPausePartie");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}
		
		response.addProperty("type", "reponseMettreEnPausePartie");
		response.addProperty("succes", true);

		String etatPartie = partie.getEtat().toString();
		dataObject.addProperty("etatPartie", etatPartie);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		return;
	}
	
}
