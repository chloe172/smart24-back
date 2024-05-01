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
		JsonObject dataObject = new JsonObject();
		response.addProperty("type", "reponseListerPlateauxPartie");

		MaitreDuJeu maitreDuJeu;

		try {
			maitreDuJeu = playITService.trouverMaitreDuJeuParId(idMaitreDuJeu);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", "Maitre du jeu non trouvé");
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonElement nomPartieObjet = data.get("nomPartie");
		String nomPartie = nomPartieObjet.getAsString();

		JsonArray listePlateauxJson = data.get("plateaux")
		                                  .getAsJsonArray();
		List<Plateau> listePlateaux = new ArrayList<>();

		for (JsonElement plateauJson : listePlateauxJson) {
			Long idPlateau = plateauJson.getAsLong();
			Plateau plateau = playITService.trouverPlateauParId(idPlateau);
			listePlateaux.add(plateau);
		}

		Partie partie;
		try {
			partie = playITService.creerPartie(nomPartie, maitreDuJeu, listePlateaux);
			session.getAttributes()
			       .put("idPartie", partie.getId());
		} catch (IllegalStateException e) {
			response.addProperty("messageErreur", "Partie non créée : " + e.getMessage());
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}
		AssociationSessionsParties.associerSessionMaitreDuJeuAPartie(session, partie);

		response.addProperty("type", "reponseCreerPartie");
		response.addProperty("succes", true);

		dataObject.addProperty("idPartie", partie.getId());
		String etatPartie = partie.getEtat()
		                          .toString();
		dataObject.addProperty("etatPartie", etatPartie);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);
	}

}
