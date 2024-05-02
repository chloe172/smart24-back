package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class ValiderCodePinController extends Controller {

	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.ANONYME);

		String codePin = data.get("codePin")
		                     .getAsString();

		Partie partie = null;
		try {
			partie = playITService.validerCodePin(codePin);
		} catch (NotFoundException e) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "reponseValiderCodePin");
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseValiderCodePin");
		response.addProperty("succes", true);

		JsonObject dataObject = new JsonObject();
		dataObject.addProperty("idPartie", partie.getId());
		dataObject.addProperty("nomPartie", partie.getNom());
		dataObject.addProperty("etatPartie", partie.getEtat()
		                                           .toString());
		response.add("data", dataObject);

		AssociationSessionsParties.ajouterSessionEquipeAPartie(session, partie);
		session.getAttributes()
		       .put("role", SessionRole.EQUIPE);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

	}

}
