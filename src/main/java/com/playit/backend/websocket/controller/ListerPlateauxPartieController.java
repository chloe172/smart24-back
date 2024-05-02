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
		response.addProperty("type", "reponseListerPlateaux");
		response.addProperty("succes", true);

		Partie partie;

		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Partie non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonObject dataObject = new JsonObject();
		JsonObject partieObject = new JsonObject();
		partieObject.addProperty("id", partie.getId());
		partieObject.addProperty("nom", partie.getNom());
		partieObject.addProperty("etat", partie.getEtat()
				.toString());

		JsonArray plateauxJsonArray = new JsonArray();
		for (PlateauEnCours plateauEnCours : partie.getPlateauxEnCours()) {
			Plateau plateau = plateauEnCours.getPlateau();
			JsonObject plateauJson = new JsonObject();
			plateauJson.addProperty("id", plateau.getId());
			plateauJson.addProperty("nom", plateau.getNom());
			plateauJson.addProperty("termine", plateauEnCours.estTermine());
			plateauJson.addProperty("nombreActivites", plateauEnCours.getNombreActivites());
			plateauJson.addProperty("nombreActivitesTerminees", plateauEnCours.getNombreActivitesTerminees());
			plateauxJsonArray.add(plateauJson);
		}
		partieObject.add("listePlateaux", plateauxJsonArray);
		dataObject.add("partie", partieObject);
		response.add("data", dataObject);
		response.addProperty("succes", true);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		return;
	}

}
