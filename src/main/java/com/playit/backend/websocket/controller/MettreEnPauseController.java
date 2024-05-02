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

public class MettreEnPauseController extends Controller {

	@Override
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
			response.addProperty("codeErreur", 404);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		try {
			playITService.mettreEnPausePartie(partie);
		} catch (IllegalStateException e) {
			response.addProperty("type", "reponseMettreEnPausePartie");
			response.addProperty("codeErreur", 422);
			response.addProperty("succes", false);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		response.addProperty("type", "reponseMettreEnPausePartie");
		response.addProperty("succes", true);

		String etatPartie = partie.getEtat()
		                          .toString();
		dataObject.addProperty("etatPartie", etatPartie);
		dataObject.addProperty("idPartie", idPartie);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		response.addProperty("type", "notificationMettreEnPausePartie");
		List<WebSocketSession> sessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);
		responseMessage = new TextMessage(response.toString());
		for (WebSocketSession sessionEquipe : sessionsEquipes) {
			sessionEquipe.getAttributes()
			             .remove("idEquipe");
			sessionEquipe.getAttributes()
			             .put("role", SessionRole.ANONYME);
			sessionEquipe.sendMessage(responseMessage);
		}

		session.getAttributes()
		       .remove("idPartie");

		AssociationSessionsParties.enleverPartie(partie);
		
		return;
	}

}
