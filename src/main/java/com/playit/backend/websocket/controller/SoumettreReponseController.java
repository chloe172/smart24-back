package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.metier.model.ActiviteEnCours;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Proposition;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class SoumettreReponseController extends Controller {
	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.EQUIPE);

		Long idPartie = data.get("idPartie")
		                    .getAsLong();
		Long idProposition = data.get("idProposition")
		                         .getAsLong();
		Long idEquipe = (Long) session.getAttributes()
		                              .get("idEquipe");
		Long idActiviteEnCours = data.get("idActiviteEnCours")
		                             .getAsLong();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseSoumettreReponse");
		Partie partie = null;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", "Partie non trouvée");
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Proposition proposition = null;
		try {
			proposition = playITService.trouverPropositionParId(idProposition);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", "Proposition non trouvée");
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Equipe equipe = null;
		try {
			equipe = playITService.trouverEquipeParId(idEquipe);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", "Equipe non trouvée");
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		ActiviteEnCours activiteEnCours = null;
		try {
			activiteEnCours = playITService.trouverActiviteEnCoursParId(idActiviteEnCours);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", "Activité en cours non trouvée");
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		int score = 0;
		try {
			score = playITService.soumettreReponse(partie, equipe, proposition, activiteEnCours);
		} catch (IllegalStateException | IllegalArgumentException e) {
			response.addProperty("messageErreur", e.getMessage());
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		response.addProperty("succes", true);

		JsonObject dataObject = new JsonObject();
		dataObject.addProperty("scoreQuestion", score);
		dataObject.addProperty("scoreEquipe", equipe.getScore());
		dataObject.addProperty("idEquipe", equipe.getId());
		dataObject.addProperty("proposition", proposition.getIntitule());
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		response.addProperty("type", "notificationSoumettreReponse");
		WebSocketSession sessionMaitreDuJeu = AssociationSessionsParties.getMaitreDuJeuPartie(equipe.getPartie());
		sessionMaitreDuJeu.sendMessage(responseMessage);

		return;
	}
}
