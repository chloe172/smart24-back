package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class ChoisirPlateauController extends Controller {

	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		Long idPartie = data.get("idPartie").getAsLong();
		Long idPlateau = data.get("idPlateau").getAsLong();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseChoisirPlateau");
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

		Plateau plateau;
		try {
			plateau = playITService.trouverPlateauParId(idPlateau);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", "Plateau non trouvé");
			response.addProperty("codeErreur", 404);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		try {
			playITService.choisirPlateau(partie, plateau);
		} catch (IllegalArgumentException | IllegalStateException e) {
			response.addProperty("messageErreur", e.getMessage());
			response.addProperty("codeErreur", 422);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonObject plateauObject = new JsonObject();
		plateauObject.addProperty("id", plateau.getId());
		plateauObject.addProperty("nom", plateau.getNom());
		JsonObject dataObject = new JsonObject();
		dataObject.add("plateau", plateauObject);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		response.addProperty("type", "notificationChoisirPlateau");
		responseMessage = new TextMessage(response.toString());
		List<WebSocketSession> listeSocketSessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);

		for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
			sessionEquipe.sendMessage(responseMessage);
		}

		return;
	}
}
