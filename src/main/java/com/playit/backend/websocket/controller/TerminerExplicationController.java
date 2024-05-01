package com.playit.backend.websocket.controller;


import com.google.gson.JsonObject;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.playit.backend.model.Partie;
import com.playit.backend.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;
import com.playit.backend.service.NotFoundException;

public class TerminerExplicationController extends Controller {

    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

        Long idPartie = (Long) data.get("idPartie").getAsLong();
        
		JsonObject response = new JsonObject();
		JsonObject dataObject = new JsonObject();
		response.addProperty("type", "reponseTerminerExplication");

		Partie partie;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", e.getMessage());
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

        // Vérification fin plateau
		if (partie.getPlateauCourant().getListeActivites().size() == partie.getIndiceActivite()) {
			try {
				playITService.passerEnModeChoixPlateau(partie);
				response.addProperty("type", "reponseTerminerExplication");
				dataObject.addProperty("finPlateau", true);
				String etatPartie = partie.getEtat().toString();
				dataObject.addProperty("etatPartie", etatPartie);
				response.add("data", dataObject);
				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);
				return;
			} catch (IllegalStateException e) {
				response.addProperty("type", "reponseTerminerExplication");
				response.addProperty("messageErreur", e.getMessage());
				response.addProperty("succes", false);
				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);
				return;
			}
		}

        try {
            playITService.terminerExpliquation(partie);
        } catch (Exception e) {
			response.addProperty("succes", false);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
        }
		response.addProperty("succes", true);
        dataObject.addProperty("finPlateau", false);
		dataObject.addProperty("idPartie", partie.getId());
		String etatPartie = partie.getEtat().toString();
		dataObject.addProperty("etatPartie", etatPartie);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		// TODO : envoyer aussi aux équipes

		return;
	}
	
}

