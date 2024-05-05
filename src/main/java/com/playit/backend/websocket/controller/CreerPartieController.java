package com.playit.backend.websocket.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.MaitreDuJeu;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class CreerPartieController extends Controller {

	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		Long idMaitreDuJeu = (Long) session.getAttributes()
				.get("idMaitreDuJeu");

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseCreerPartie");
		response.addProperty("succes", true);

		MaitreDuJeu maitreDuJeu;
		try {
			maitreDuJeu = playITService.trouverMaitreDuJeuParId(idMaitreDuJeu);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", "Maitre du jeu non trouvé");
			response.addProperty("codeErreur", 404);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		String nomPartie = data.get("nomPartie").getAsString();
		JsonArray listePlateauxJson = data.get("plateaux")
				.getAsJsonArray();
		List<Plateau> listePlateaux = new ArrayList<>();

		for (JsonElement plateauJson : listePlateauxJson) {
			Long idPlateau = plateauJson.getAsLong();
			Plateau plateau = null;
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
			listePlateaux.add(plateau);
		}

		Partie partie;
		try {
			partie = playITService.creerPartie(nomPartie, maitreDuJeu, listePlateaux);
		} catch (IllegalStateException e) {
			response.addProperty("messageErreur", "Partie non créée : " + e.getMessage());
			response.addProperty("codeErreur", 422);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}
		session.getAttributes().put("idPartie", partie.getId());
		AssociationSessionsParties.associerSessionMaitreDuJeuAPartie(session, partie);
		AssociationSessionsParties.ajouterPartie(partie);

		JsonObject partieObject = new JsonObject();
		partieObject.addProperty("id", partie.getId());
		partieObject.addProperty("nom", partie.getNom());
		partieObject.addProperty("etat", partie.getEtat().toString());
		partieObject.addProperty("codePin", partie.getCodePin());
		partieObject.addProperty("date", partie.getDate().toString());
		JsonObject dataObject = new JsonObject();
		dataObject.add("partie", partieObject);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);
		return;
	}

}
