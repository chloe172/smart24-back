package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class TerminerExplicationController extends Controller {

	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		Long idPartie = data.get("idPartie")
				.getAsLong();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseTerminerExplication");
		response.addProperty("succes", true);

		Partie partie;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		List<Equipe> listeEquipes = null;

		// Vérification fin plateau
		boolean finPlateau = partie.getPlateauCourant().estTermine();
		if (partie.getPlateauCourant().estTermine()) {
			try {
				playITService.passerEnModeChoixPlateau(partie);
			} catch (IllegalStateException e) {
				response.addProperty("succes", false);
				response.addProperty("codeErreur", 422);
				response.addProperty("messageErreur", e.getMessage());
				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);
				return;
			}
		} else {
			try {
				playITService.terminerExpliquation(partie);
			} catch (Exception e) {
				response.addProperty("succes", false);
				response.addProperty("codeErreur", 422);
				response.addProperty("messageErreur", e.getMessage());
				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);
				return;
			}
		}

		listeEquipes = playITService.obtenirEquipesParRang(partie);

		JsonObject partieObject = new JsonObject();
		partieObject.addProperty("id", partie.getId());
		partieObject.addProperty("nom", partie.getNom());
		partieObject.addProperty("etat", partie.getEtat()
				.toString());
		partieObject.addProperty("date", partie.getDate()
				.toString());
		partieObject.addProperty("finPlateau", finPlateau);
		JsonObject dataObject = new JsonObject();
		dataObject.add("partie", partieObject);

		JsonArray listeEquipesJson = new JsonArray();
		for (int i = 0; i < listeEquipes.size(); i++) {
			Equipe equipe = listeEquipes.get(i);
			JsonObject equipeJson = new JsonObject();
			equipeJson.addProperty("id", equipe.getId());
			equipeJson.addProperty("nom", equipe.getNom());
			equipeJson.addProperty("score", equipe.getScore());
			equipeJson.addProperty("rang", i + 1);
			listeEquipesJson.add(equipeJson);
		}
		dataObject.add("listeEquipes", listeEquipesJson);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		response.addProperty("type", "notificationTerminerExplication");
		responseMessage = new TextMessage(response.toString());
		List<WebSocketSession> listeSocketSessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);

		for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
			sessionEquipe.sendMessage(responseMessage);
		}

		return;

	}

}
