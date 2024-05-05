package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.model.PlateauEnCours;
import com.playit.backend.metier.model.ScorePlateau;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class ChoisirPlateauController extends Controller {

	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
		this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

		Long idPartie = data.get("idPartie").getAsLong();
		Long idPlateau = data.get("idPlateau").getAsLong();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseChoisirPlateau");
		response.addProperty("succes", true);

		Partie partie;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("messageErreur", "Partie non trouvée");
			response.addProperty("codeErreur", 404);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Plateau plateau;
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
		
		PlateauEnCours plateauEnCours = partie.getPlateauxEnCours().
									stream().filter(p -> p.getPlateau().getId().equals(idPlateau))
									.findFirst()
									.orElse(null);
		
		if(plateauEnCours==null) {
			response.addProperty("messageErreur", "Le plateau ne fait pas partie de la partie");
			response.addProperty("codeErreur", 422);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		} 

		if(plateauEnCours.estTermine()) {
			JsonObject plateauObject = new JsonObject();
			plateauObject.addProperty("id", plateau.getId());
			plateauObject.addProperty("nom", plateau.getNom());
			plateauObject.addProperty("termine", plateauEnCours.estTermine());
			JsonObject dataObject = new JsonObject();
			dataObject.add("plateau", plateauObject);
			
			JsonArray listeEquipesJson = new JsonArray();
			List<Equipe> listeEquipes = playITService.obtenirEquipesParRang(partie);
			List<ScorePlateau> listeScore = playITService.obtenirEquipesParRang(partie, plateau);
			for (int i = 0; i < listeEquipes.size(); i++) {
				Equipe equipe = listeEquipes.get(i);
				JsonObject equipeJson = new JsonObject();
				equipeJson.addProperty("id", equipe.getId());
				equipeJson.addProperty("nom", equipe.getNom());
				int score = listeScore.stream()
						.filter(scorePlateau -> scorePlateau.getEquipe().getId().equals(equipe.getId()))
						.findFirst()
						.map(ScorePlateau::getScore)
						.orElse(0);
				equipeJson.addProperty("score", score);
				equipeJson.addProperty("avatar", equipe.getAvatar().toString());
				if (i == 0) {
					equipeJson.addProperty("rang", "1er");
				} else {
					equipeJson.addProperty("rang", i + 1 + "ème");
				}
				listeEquipesJson.add(equipeJson);
			}
			dataObject.add("listeEquipes", listeEquipesJson);
			response.add("data", dataObject);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		try {
			playITService.choisirPlateau(partie, plateau);
		} catch (IllegalArgumentException | IllegalStateException e) {
			response.addProperty("messageErreur", e.getMessage());
			response.addProperty("codeErreur", 422);
			response.addProperty("succes", false);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		JsonObject plateauObject = new JsonObject();
		plateauObject.addProperty("id", plateau.getId());
		plateauObject.addProperty("nom", plateau.getNom());
		plateauObject.addProperty("termine", plateauEnCours.estTermine());
		JsonObject dataObject = new JsonObject();
		dataObject.add("plateau", plateauObject);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		response.addProperty("type", "notificationChoisirPlateau");
		responseMessage = new TextMessage(response.toString());
		List<WebSocketSession> listeSocketSessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);

		for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
			sessionEquipe.sendMessage(responseMessage);
		}

		return;
	}
}
