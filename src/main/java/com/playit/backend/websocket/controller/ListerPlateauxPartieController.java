package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.model.PlateauEnCours;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;

public class ListerPlateauxPartieController extends Controller {

	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {

		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		Long idPartie = data.get("idPartie")
		                    .getAsLong();

		JsonObject response = new JsonObject();
		JsonObject dataObject = new JsonObject();
		response.addProperty("type", "reponseListerPlateaux");

		Partie partie;

		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Partie non trouvée");
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonArray listePlateauxJson = new JsonArray();
		for (PlateauEnCours plateauEnCours : partie.getPlateauxEnCours()) {
			Plateau plateau = plateauEnCours.getPlateau();
			JsonObject plateauJson = new JsonObject();
			plateauJson.addProperty("nom", plateau.getNom());
			plateauJson.addProperty("id", plateau.getId());
			plateauJson.addProperty("termine", plateauEnCours.estTermine());
			plateauJson.addProperty("nombreActivites", plateauEnCours.getNombreActivites());
			plateauJson.addProperty("nombreActivitesTerminees", plateauEnCours.getNombreActivitesTerminees());
			listePlateauxJson.add(plateauJson);
		}
		dataObject.addProperty("idPartie", partie.getId());
		dataObject.add("listePlateaux", listePlateauxJson);
		response.add("data", dataObject);
		response.addProperty("succes", true);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		return;
	}

}
