package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class ModifierEquipeController extends Controller {

	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.EQUIPE);

		Long idEquipe = data.get("idEquipe").getAsLong();
		String nouveauNomEquipe = data.get("nouveauNomEquipe").getAsString();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseModifierEquipe");
		response.addProperty("succes", true);

		Equipe equipe = null;
		try {
			equipe = playITService.trouverEquipeParId(idEquipe);
		} catch (Exception e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Equipe non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		try {
			equipe = playITService.modifierEquipe(equipe, nouveauNomEquipe);
		} catch (IllegalStateException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 422);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonObject equipeObject = new JsonObject();
		equipeObject.addProperty("id", equipe.getId());
		equipeObject.addProperty("nom", equipe.getNom());
		equipeObject.addProperty("avatar", equipe.getAvatar().toString());
		JsonObject dataObject = new JsonObject();
		dataObject.add("equipe", equipeObject);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		response.addProperty("type", "notificationModifierEquipe");
		WebSocketSession sessionMaitreDuJeu = AssociationSessionsParties.getMaitreDuJeuPartie(equipe.getPartie());
		sessionMaitreDuJeu.sendMessage(responseMessage);

		return;
	}
}
