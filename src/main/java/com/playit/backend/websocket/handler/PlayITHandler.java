package com.playit.backend.websocket.handler;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.EtatPartie;
import com.playit.backend.metier.model.MaitreDuJeu;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.repository.EquipeRepository;
import com.playit.backend.repository.PartieRepository;
import com.playit.backend.websocket.RoleUtilisateurException;
import com.playit.backend.websocket.controller.AttendreEquipesController;
import com.playit.backend.websocket.controller.AuthentifierUtilisateurController;
import com.playit.backend.websocket.controller.ChoisirPlateauController;
import com.playit.backend.websocket.controller.Controller;
import com.playit.backend.websocket.controller.CreerPartieController;
import com.playit.backend.websocket.controller.DemarrerPartieController;
import com.playit.backend.websocket.controller.InscrireEquipeController;
import com.playit.backend.websocket.controller.LancerActiviteController;
import com.playit.backend.websocket.controller.ListerEquipesController;
import com.playit.backend.websocket.controller.ListerPartiesController;
import com.playit.backend.websocket.controller.ListerPlateauxController;
import com.playit.backend.websocket.controller.ListerPlateauxPartieController;
import com.playit.backend.websocket.controller.MettreEnPauseController;
import com.playit.backend.websocket.controller.ModifierEquipeController;
import com.playit.backend.websocket.controller.RejoindrePartieEquipeController;
import com.playit.backend.websocket.controller.SoumettreReponseController;
import com.playit.backend.websocket.controller.SoumettreScoreMiniJeuController;
import com.playit.backend.websocket.controller.TerminerExplicationController;
import com.playit.backend.websocket.controller.TerminerPartieController;
import com.playit.backend.websocket.controller.TokenController;
import com.playit.backend.websocket.controller.ValiderCodePinController;

@Component
public class PlayITHandler extends TextWebSocketHandler {

	@Autowired
	PlayITService playITService;
	@Autowired
	PartieRepository partieRepository;
	@Autowired
	EquipeRepository equipeRepository;

	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) throws IOException {
		System.out.println("New session: " + session.getId());
		session.getAttributes()
				.put("role", SessionRole.ANONYME);
	}

	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status)
			throws IOException {
		System.out.println("Session closed: " + session.getId());
		AssociationSessionsParties.retirerSession(session);

		SessionRole role = (SessionRole) session.getAttributes()
				.get("role");
		if (role == SessionRole.MAITRE_DU_JEU) {
			Long idMaitreDuJeu = (Long) session.getAttributes()
					.get("idMaitreDuJeu");
			MaitreDuJeu maitreDuJeu = playITService.trouverMaitreDuJeuParId(idMaitreDuJeu);
			Partie partieCourante = maitreDuJeu.getListeParties()
					.stream()
					.filter(partie -> partie.getEtat() != EtatPartie.TERMINEE
							&& partie.getEtat() != EtatPartie.EN_PAUSE)
					.findFirst()
					.orElse(null);
			if (partieCourante != null) {
				partieCourante.setEtat(EtatPartie.EN_PAUSE);
				partieCourante = partieRepository.saveAndFlush(partieCourante);

				JsonObject response = new JsonObject();
				JsonObject dataObject = new JsonObject();
				response.addProperty("type", "notificationMettreEnPausePartie");
				response.addProperty("succes", true);
				response.add("data", dataObject);
				List<WebSocketSession> sessionsEquipes = AssociationSessionsParties
						.getEquipesParPartie(partieCourante);
				TextMessage responseMessage = new TextMessage(response.toString());
				for (WebSocketSession sessionEquipe : sessionsEquipes) {
					Long idEquipe = (Long) sessionEquipe.getAttributes().get("idEquipe");
					Equipe equipe = playITService.trouverEquipeParId(idEquipe);
					equipe.setEstConnecte(false);
					equipeRepository.saveAndFlush(equipe);
					sessionEquipe.getAttributes()
							.remove("idEquipe");
					sessionEquipe.getAttributes()
							.put("role", SessionRole.ANONYME);
					sessionEquipe.sendMessage(responseMessage);
				}
			}
		} else if (role == SessionRole.EQUIPE) {
			Long idEquipe = (Long) session.getAttributes()
					.get("idEquipe");
			Equipe equipe = playITService.trouverEquipeParId(idEquipe);
			equipe.setEstConnecte(false);
			equipeRepository.saveAndFlush(equipe);
			session.getAttributes().put("role", SessionRole.ANONYME);
			session.getAttributes().remove("idEquipe");
		}
	}

	@Override
	protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
		System.out.println("Message received from " + session.getId());
		System.out.println("Message: " + message.getPayload());

		JsonObject body = null;
		String type = null;
		JsonObject data = null;
		try {
			body = new Gson().fromJson(message.getPayload(), JsonObject.class);
			type = body.get("type")
					.getAsString();
			data = body.getAsJsonObject("data");
		} catch (JsonSyntaxException e) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "erreur");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", "Message invalide : JSON invalide");
			response.addProperty("codeErreur", 400);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		} catch (NullPointerException e) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "erreur");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", "Message invalide : type ou data manquant");
			response.addProperty("codeErreur", 400);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		} catch (ClassCastException | UnsupportedOperationException e) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "erreur");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", "Message invalide : type ou data invalide");
			response.addProperty("codeErreur", 400);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
			return;
		}

		Controller controller = null;
		switch (type) {
			case "authentifierUtilisateur": {
				controller = new AuthentifierUtilisateurController();
				break;
			}
			case "listerPlateaux": {
				controller = new ListerPlateauxController();
				break;
			}
			case "listerPlateauxPartie": {
				controller = new ListerPlateauxPartieController();
				break;
			}
			case "creerPartie": {
				controller = new CreerPartieController();
				break;
			}
			case "listerParties": {
				controller = new ListerPartiesController();
				break;
			}
			case "attendreEquipes": {
				controller = new AttendreEquipesController();
				break;
			}
			case "choisirPlateau": {
				controller = new ChoisirPlateauController();
				break;
			}
			case "lancerActivite": {
				controller = new LancerActiviteController();
				break;
			}
			case "mettreEnPause": {
				controller = new MettreEnPauseController();
				break;
			}
			case "terminerPartie": {
				controller = new TerminerPartieController();
				break;
			}
			case "listerEquipes": {
				controller = new ListerEquipesController();
				break;
			}
			case "inscrireEquipe": {
				controller = new InscrireEquipeController();
				break;
			}
			case "soumettreReponse": {
				controller = new SoumettreReponseController();
				break;
			}
			case "modifierEquipe": {
				controller = new ModifierEquipeController();
				break;
			}
			case "validerCodePin": {
				controller = new ValiderCodePinController();
				break;
			}
			case "demarrerPartie": {
				controller = new DemarrerPartieController();
				break;
			}
			case "terminerExplication": {
				controller = new TerminerExplicationController();
				break;
			}
			case "rejoindrePartieEquipe": {
				controller = new RejoindrePartieEquipeController();
				break;
			}
			case "soumettreScoreMinijeu": {
				controller = new SoumettreScoreMiniJeuController();
				break;
			}
			case "token": {
				controller = new TokenController();
				break;
			}
		}

		if (controller != null) {
			try {
				controller.handleRequest(session, data, this.playITService);
			} catch (RoleUtilisateurException e) {
				String reponseType = "reponse" + type.substring(0, 1)
						.toUpperCase()
						+ type.substring(1);

				JsonObject response = new JsonObject();
				response.addProperty("type", reponseType);
				response.addProperty("succes", false);
				response.addProperty("codeErreur", 403);
				response.addProperty("messageErreur", "Vous n'avez pas les droits pour effectuer cette action");
				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			JsonObject response = new JsonObject();
			response.addProperty("type", "erreur");
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 400);
			response.addProperty("messageErreur", "Type de message inconnu : " + type);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
		}
	}
}