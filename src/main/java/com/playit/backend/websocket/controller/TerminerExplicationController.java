package com.playit.backend.websocket.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		// Vérification fin plateau
		boolean finPlateau = partie.getPlateauCourant().estTermine();
		if (finPlateau) {
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

		Plateau plateau = partie.getPlateauCourant().getPlateau();
		Map<Plateau, List<ScorePlateau>> mapScore = new HashMap<>();
		for (Plateau p : partie.getPlateauxEnCours().stream()
				.map(PlateauEnCours::getPlateau)
				.toList()) {
			List<ScorePlateau> listeScore = playITService.obtenirEquipesParRang(partie, p);
			mapScore.put(p, listeScore);
		}

		JsonObject partieObject = new JsonObject();
		partieObject.addProperty("id", partie.getId());
		partieObject.addProperty("nom", partie.getNom());
		partieObject.addProperty("etat", partie.getEtat()
				.toString());
		partieObject.addProperty("date", partie.getDate()
				.toString());
		partieObject.addProperty("finPlateau", finPlateau);
		JsonObject dataObjectMdj = new JsonObject();
		dataObjectMdj.add("partie", partieObject);

		JsonArray listeEquipesJson = new JsonArray();
		List<ScorePlateau> listeScorePlateauEnCours = mapScore.get(plateau);
		int i = 1;
		for (ScorePlateau score : listeScorePlateauEnCours) {
			JsonObject equipeJson = new JsonObject();
			equipeJson.addProperty("id", score.getEquipe()
					.getId());
			equipeJson.addProperty("nom", score.getEquipe()
					.getNom());
			equipeJson.addProperty("score", score.getScore());
			if(score.getRang()==1){
				equipeJson.addProperty("rang", score.getRang()+ "er");
			} else {
				equipeJson.addProperty("rang", score.getRang()+"ème");
			}
			equipeJson.addProperty("avatar", score.getEquipe()
					.getAvatar().toString());
			listeEquipesJson.add(equipeJson);
			i++;
		}
		dataObjectMdj.add("listeEquipes", listeEquipesJson);
		response.add("data", dataObjectMdj);
		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		// Envoi aux équipes :

		JsonObject dataObjectEquipe = new JsonObject();
		dataObjectEquipe.add("partie", partieObject);
		List<WebSocketSession> listeSocketSessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);
		for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
			Long idEquipe = (Long) sessionEquipe.getAttributes()
					.get("idEquipe");
			Equipe equipe = playITService.trouverEquipeParId(idEquipe);

			JsonObject equipeObject = new JsonObject();
			equipeObject.addProperty("id", equipe.getId());
			equipeObject.addProperty("nom", equipe.getNom());
			equipeObject.addProperty("score", equipe.getScore());
			equipeObject.addProperty("avatar", equipe
					.getAvatar().toString());
			int rang = listeScorePlateauEnCours.stream()
					.filter(s -> s.getEquipe()
							.getId()
							.equals(idEquipe))
					.findFirst()
					.map(ScorePlateau::getRang)
					.orElse(0);
			if (rang == 1) {
				equipeObject.addProperty("rang", "1er");
			} else {
				equipeObject.addProperty("rang", rang + "ème");
			}
			JsonArray badgesArray = new JsonArray();
			for (Map.Entry<Plateau, List<ScorePlateau>> entry : mapScore.entrySet()) {
				Plateau p = entry.getKey();
				List<ScorePlateau> listeScore = entry.getValue();
				ScorePlateau score = listeScore.stream()
						.filter(s -> s.getEquipe()
								.getId()
								.equals(idEquipe))
						.findFirst()
						.orElse(null);

				JsonObject badgeObject = new JsonObject();
				badgeObject.addProperty("plateau", p.getNom());

				PlateauEnCours pec = partie.getPlateauxEnCours()
						.stream()
						.filter(p1 -> p1.getPlateau().getId().equals(p.getId()))
						.findFirst()
						.orElse(null);
				if (score != null && pec.estTermine()) {
					badgeObject.addProperty("rang", score.getCouleurBadge());
				} else {
					badgeObject.addProperty("rang", "noir");
				}
				badgesArray.add(badgeObject);
			}
			equipeObject.add("badges", badgesArray);
			dataObjectEquipe.add("equipe", equipeObject);
			response.add("data", dataObjectEquipe);
			response.addProperty("type", "notificationTerminerExplication");
			responseMessage = new TextMessage(response.toString());
			sessionEquipe.sendMessage(responseMessage);
		}

		return;

	}

}
