package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

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

		Long idPartie = data.get("idPartie").getAsLong();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseMettreEnPausePartie");
		response.addProperty("succes", true);

		Partie partie = null;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Partie non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		try {
			playITService.mettreEnPausePartie(partie);
		} catch (IllegalStateException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 422);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonObject partieObject = new JsonObject();
		partieObject.addProperty("id", partie.getId());
		partieObject.addProperty("nom", partie.getNom());
		partieObject.addProperty("etat", partie.getEtat()
				.toString());
		partieObject.addProperty("codePin", partie.getCodePin());
		partieObject.addProperty("date", partie.getDate()
				.toString());
		JsonObject dataObject = new JsonObject();
		dataObject.add("partie", partieObject);
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
