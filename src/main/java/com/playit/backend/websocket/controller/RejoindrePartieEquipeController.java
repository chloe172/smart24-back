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

public class RejoindrePartieEquipeController extends Controller {

	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.EQUIPE);
		Long idPartie = data.get("idPartie")
				.getAsLong();
		Long idEquipe = data.get("idEquipe")
				.getAsLong();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseRejoindrePartieEquipe");
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

		Equipe equipe = null;
		try {
			equipe = playITService.trouverEquipeParId(idEquipe);
		} catch (NotFoundException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Equipe non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}
		try {
			equipe = playITService.rejoindrePartieEquipe(equipe, partie);
		} catch (Exception e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 422);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		session.getAttributes()
				.put("idEquipe", equipe.getId());

		JsonObject equipeObject = new JsonObject();
		equipeObject.addProperty("id", equipe.getId());
		equipeObject.addProperty("nom", equipe.getNom());
		JsonObject dataObject = new JsonObject();
		dataObject.add("equipe", equipeObject);

		JsonObject partieObject = new JsonObject();
		partieObject.addProperty("id", partie.getId());
		partieObject.addProperty("nom", partie.getNom());
		partieObject.addProperty("etat", partie.getEtat()
				.toString());
		partieObject.addProperty("date", partie.getDate()
				.toString());
		dataObject.add("partie", partieObject);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		// ATTENTION : ne pas changer le type !!
		response.addProperty("type", "notificationInscrireEquipe");
		WebSocketSession sessionMaitreDuJeu = AssociationSessionsParties.getMaitreDuJeuPartie(partie);
		responseMessage = new TextMessage(response.toString());
		sessionMaitreDuJeu.sendMessage(responseMessage);

		List<WebSocketSession> sessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);
		for (WebSocketSession sessionEquipe : sessionsEquipes) {
			if (session != sessionEquipe) {
				sessionEquipe.sendMessage(responseMessage);
			}
		}

		return;
	}

}
