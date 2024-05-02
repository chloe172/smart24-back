package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;

public class ListerEquipesController extends Controller {
	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.EQUIPE);

		Long idPartie = data.get("idPartie")
				.getAsLong();
		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseListerEquipes");
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

		JsonObject dataObject = new JsonObject();
		List<Equipe> listeEquipes = partie.getEquipes();
		JsonArray listeEquipesConnecteesJson = new JsonArray();
		JsonArray listeEquipesNonConnecteesJson = new JsonArray();
		for (Equipe equipe : listeEquipes) {
			JsonObject equipeJson = new JsonObject();
			equipeJson.addProperty("id", equipe.getId());
			equipeJson.addProperty("nom", equipe.getNom());
			equipeJson.addProperty("score", equipe.getScore());
			if (equipe.getEstConnecte()) {
				listeEquipesConnecteesJson.add(equipeJson);
			} else {
				listeEquipesNonConnecteesJson.add(equipeJson);
			}
		}
		dataObject.add("listeEquipesConnectees", listeEquipesConnecteesJson);
		dataObject.add("listeEquipesNonConnectees", listeEquipesNonConnecteesJson);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		return;
	}

}
