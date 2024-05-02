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
		System.out.println("ListerEquipesController");
		System.out.println(session.getAttributes().get("role"));
		this.userHasRoleOrThrow(session, SessionRole.EQUIPE);

		Long idPartie = data.get("idPartie")
		                    .getAsLong();

		Partie partie = null;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "reponseListerEquipes");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", "Partie non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseListerEquipes");
		response.addProperty("succes", true);

		JsonObject dataObject = new JsonObject();
		List<Equipe> listeEquipes = partie.getEquipes();
		JsonArray listeEquipesJson = new JsonArray();
		for (Equipe equipe : listeEquipes) {
			JsonObject equipeJson = new JsonObject();
			equipeJson.addProperty("nomEquipe", equipe.getNom());
			equipeJson.addProperty("scoreEquipe", equipe.getScore());
			equipeJson.addProperty("idEquipe", equipe.getId());
			listeEquipesJson.add(equipeJson);
		}
		dataObject.add("listeEquipes", listeEquipesJson);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		return;
	}

}
