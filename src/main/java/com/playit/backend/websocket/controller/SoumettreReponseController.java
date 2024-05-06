package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.ActiviteEnCours;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.model.Proposition;
import com.playit.backend.metier.model.Question;
import com.playit.backend.metier.model.ScorePlateau;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.PartieThreadAttente;
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
		response.addProperty("succes", true);

		Partie partie = null;
		try {
			partie = playITService.trouverPartieParId(idPartie);
		} catch (NotFoundException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Partie non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Proposition proposition = null;
		try {
			proposition = playITService.trouverPropositionParId(idProposition);
		} catch (NotFoundException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Proposition non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Equipe equipe = null;
		try {
			equipe = playITService.trouverEquipeParId(idEquipe);
		} catch (NotFoundException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Equipe non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		ActiviteEnCours activiteEnCours = null;
		try {
			activiteEnCours = playITService.trouverActiviteEnCoursParId(idActiviteEnCours);
		} catch (NotFoundException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 404);
			response.addProperty("messageErreur", "Activité en cours non trouvée");
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		int score = 0;
		try {
			score = playITService.soumettreReponse(partie, equipe, proposition, activiteEnCours);
			System.out.println("score BD"+score);
		} catch (IllegalStateException | IllegalArgumentException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 422);
			response.addProperty("messageErreur", e.getMessage());
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		boolean arreter = playITService.verifierSoumissionParToutesLesEquipes(activiteEnCours);
		if (arreter) {
			System.out.println("Toutes les équipes ont soumis leur réponse");
			PartieThreadAttente.stopThread(partie);
			JsonObject notification = new JsonObject();
			notification.addProperty("type", "notificationReponseActivite");
			notification.addProperty("succes", true);

			playITService.passerEnModeExplication(partie);
			Question question = (Question) activiteEnCours.getActivite();

			JsonObject questionJson = new JsonObject();
			questionJson.addProperty("id", question.getId());
			questionJson.addProperty("intitule", question.getIntitule());
			questionJson.addProperty("temps", question.getTemps()
					.toSeconds());

			JsonArray listePropositionsJson = new JsonArray();
			List<Proposition> listePropositions = question.getListePropositions();

			for (Proposition p : listePropositions) {
				JsonObject propositionJson = new JsonObject();
				propositionJson.addProperty("id", p.getId());
				propositionJson.addProperty("intitule", p.getIntitule());
				listePropositionsJson.add(propositionJson);
			}
			questionJson.add("listePropositions", listePropositionsJson);
			Proposition bonneProposition = question.getBonneProposition();
			JsonObject bonnePropositionObject = new JsonObject();
			bonnePropositionObject.addProperty("id", bonneProposition.getId());
			bonnePropositionObject.addProperty("intitule", bonneProposition.getIntitule());
			questionJson.add("bonneProposition", bonnePropositionObject);
			JsonObject dataObject = new JsonObject();
			dataObject.add("question", questionJson);
			dataObject.addProperty("idActiviteEnCours", activiteEnCours.getId());
			dataObject.addProperty("typeActivite", "question");
			dataObject.add("question", questionJson);
			dataObject.addProperty("nomPlateauCourant", partie.getPlateauCourant().getPlateau().getNom());
			notification.add("data", dataObject);

			// Envoi du message aux equipes : bonne proposition uniquement
			List<WebSocketSession> listeSocketSessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);
			questionJson.add("bonneProposition", bonnePropositionObject);
			dataObject.add("question", questionJson);
			notification.add("data", dataObject);
			Plateau plateau = partie.getPlateauCourant().getPlateau();
			List<ScorePlateau> listeScores = playITService.obtenirEquipesParRang(partie, plateau);

			for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
				Long id = (Long) sessionEquipe.getAttributes().get("idEquipe");
				Equipe equipeSession = null;
				try {
					equipeSession = playITService.trouverEquipeParId(id);
				} catch (NotFoundException e) {
					continue;
				}
				final Equipe equipeSessinoFinal = equipeSession;
				JsonObject equipeJson = new JsonObject();
				equipeJson.addProperty("id", equipeSessinoFinal.getId());
				equipeJson.addProperty("nom", equipeSessinoFinal.getNom());
				equipeJson.addProperty("avatar", equipeSessinoFinal.getAvatar().toString());
				int scoreEquipePlateauCourant = listeScores.stream()
						.filter(s -> s.getEquipe()
								.getId()
								.equals(equipeSessinoFinal.getId()))
						.findFirst()
						.get()
						.getScore();
				equipeJson.addProperty("score", scoreEquipePlateauCourant);
				dataObject.add("equipe", equipeJson);
				TextMessage bonnePropositionMessage = new TextMessage(notification.toString());
				try {
					sessionEquipe.sendMessage(bonnePropositionMessage);
				} catch (Exception ex) {
				}
			}

			// Envoi au maitre du jeu : bonne proposition et explication
			questionJson.addProperty("explication", question.getExplication());
			JsonArray listeEquipesJson = new JsonArray();
			for (int i = 0; i < listeScores.size(); i++) {
				ScorePlateau scorePlateau = listeScores.get(i);
				Equipe equipeMDJ = scorePlateau.getEquipe();
				JsonObject equipeJson = new JsonObject();
				equipeJson.addProperty("id", equipeMDJ.getId());
				equipeJson.addProperty("nom", equipeMDJ.getNom());
				int scoreMDJ = listeScores.stream()
						.filter(sp -> sp.getEquipe().getId().equals(equipeMDJ.getId()))
						.findFirst()
						.map(ScorePlateau::getScore)
						.orElse(0);
				equipeJson.addProperty("score", scoreMDJ);
				equipeJson.addProperty("avatar", equipeMDJ.getAvatar().toString());
				if (i == 0) {
					equipeJson.addProperty("rang", "1er");
				} else {
					equipeJson.addProperty("rang", i + 1 + "ème");
				}
				listeEquipesJson.add(equipeJson);
			}
			dataObject.add("listeEquipes", listeEquipesJson);
			notification.add("data", dataObject);
			TextMessage bonnePropositionMessage = new TextMessage(notification.toString());
			WebSocketSession sessionMaitreDuJeu = AssociationSessionsParties.getMaitreDuJeuPartie(partie);
			sessionMaitreDuJeu.sendMessage(bonnePropositionMessage);
		}

		JsonObject reponseObject = new JsonObject();
		reponseObject.addProperty("score", score);
		JsonObject equipeObject = new JsonObject();
		equipeObject.addProperty("id", equipe.getId());
		equipeObject.addProperty("nom", equipe.getNom());
		Plateau plateau = partie.getPlateauCourant().getPlateau();
		List<ScorePlateau> listeScores = playITService.obtenirEquipesParRang(partie, plateau);
		final Equipe equipefinal = equipe;
		score = listeScores.stream()
						.filter(sp -> sp.getEquipe().getId().equals(equipefinal.getId()))
						.findFirst()
						.map(ScorePlateau::getScore)
						.orElse(0);
		equipeObject.addProperty("score", score);
		equipeObject.addProperty("avatar", equipe.getAvatar().toString());
		reponseObject.add("equipe", equipeObject);
		JsonObject propositionObject = new JsonObject();
		propositionObject.addProperty("id", proposition.getId());
		propositionObject.addProperty("intitule", proposition.getIntitule());
		reponseObject.add("proposition", propositionObject);
		JsonObject dataObject = new JsonObject();
		dataObject.add("reponse", reponseObject);
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		response.addProperty("type", "notificationSoumettreReponse");
		responseMessage = new TextMessage(response.toString());
		WebSocketSession sessionMaitreDuJeu = AssociationSessionsParties.getMaitreDuJeuPartie(equipe.getPartie());
		sessionMaitreDuJeu.sendMessage(responseMessage);

		return;
	}
}
