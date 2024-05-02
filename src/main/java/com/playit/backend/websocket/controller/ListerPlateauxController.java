package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;

public class ListerPlateauxController extends Controller {

	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseListerPlateaux");

		JsonObject dataObject = new JsonObject();
		List<Plateau> listePlateaux = playITService.listerPlateaux();
		JsonArray listePlateauxJson = new JsonArray();
		for (Plateau plateau : listePlateaux) {
			JsonObject plateauJson = new JsonObject();
			plateauJson.addProperty("nom", plateau.getNom());
			plateauJson.addProperty("id", plateau.getId());
			listePlateauxJson.add(plateauJson);
		}
		dataObject.add("listePlateaux", listePlateauxJson);
		response.add("data", dataObject);
		response.addProperty("succes", true);
		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);
	}
}
