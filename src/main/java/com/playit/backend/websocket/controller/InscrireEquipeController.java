package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Avatar;
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

		Long idEquipe = (Long) session.getAttributes()
				.get("idEquipe");
		if (idEquipe != null) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "reponseInscrireEquipe");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", "Vous êtes déjà inscrit dans une équipe");
			response.addProperty("codeErreur", 422);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Long idPartie = data.get("idPartie")
				.getAsLong();
		String nomEquipe = data.get("nomEquipe")
				.getAsString();
		String avatarName = data.get("avatar")
				.getAsString();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseInscrireEquipe");
		response.addProperty("succes", true);

		Partie partie = null;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("succes", false);
			response.addProperty("messageErreur", "Partie non trouvée");
			response.addProperty("codeErreur", 404);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Equipe equipe = null;
		try {
			Avatar avatar = Avatar.valueOf(avatarName);
			equipe = playITService.inscrireEquipe(nomEquipe, avatar, partie);
		} catch (Exception e) {
			response.addProperty("succes", false);
			response.addProperty("messageErreur", e.getMessage());
			response.addProperty("codeErreur", 422);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		session.getAttributes()
				.put("idEquipe", equipe.getId());

		JsonObject partieObject = new JsonObject();
		partieObject.addProperty("id", partie.getId());
		partieObject.addProperty("nom", partie.getNom());
		JsonObject dataObject = new JsonObject();
		dataObject.add("partie", partieObject);

		JsonObject equipeObject = new JsonObject();
		equipeObject.addProperty("id", equipe.getId());
		equipeObject.addProperty("nom", equipe.getNom());
		dataObject.add("equipe", equipeObject);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

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
