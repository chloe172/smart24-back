package com.playit.backend.websocket.handler;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.playit.backend.model.MaitreDuJeu;
import com.playit.backend.model.Partie;
import com.playit.backend.model.Plateau;
import com.playit.backend.model.Proposition;
import com.playit.backend.model.Question;
import com.playit.backend.model.QuestionQCM;
import com.playit.backend.model.QuestionVraiFaux;
import com.playit.backend.model.Activite;
import com.playit.backend.model.ActiviteEnCours;
import com.playit.backend.model.Equipe;
import com.playit.backend.service.PlayITService;
import com.playit.backend.websocket.controller.Controller;
import com.playit.backend.websocket.controller.CreerPartieController;
import com.playit.backend.websocket.controller.ListerParties;
import com.playit.backend.websocket.controller.ListerPlateauxController;
import com.playit.backend.websocket.controller.ListerPlateauxPartieController;
import com.playit.backend.websocket.RoleUtilisateurException;
import com.playit.backend.websocket.controller.AuthentifierUtilisateurController;

@Component
public class PlayITHandler extends TextWebSocketHandler {

	@Autowired
	PlayITService playITService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws IOException {
		System.out.println("New session: " + session.getId());
		session.getAttributes().put("role", SessionRole.ANONYME);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		System.out.println("Session closed: " + session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		System.out.println("Message received from " + session.getId());
		System.out.println("Message: " + message.getPayload());

		// TODO : penser à gérer les exceptions
		JsonObject body = new Gson().fromJson(message.getPayload(), JsonObject.class);
		JsonElement typeElement = body.get("type");

		String type = typeElement.getAsString();
		JsonObject data = body.get("data").getAsJsonObject();

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
				controller = new ListerParties();
				break;
			}
			case "demarrerPartie": {
				JsonElement idPartieObjet = data.get("idPartie");
				Long idPartie = idPartieObjet.getAsLong();

				// TODO : reception exception - pas d'id dans BD
				Partie partie = playITService.trouverPartieParId(idPartie);
				playITService.demarrerPartie(partie);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseDemarrerPartie");
				response.addProperty("succes", true);

				String etatPartie = partie.getEtat().toString();
				JsonObject dataObject = new JsonObject();
				dataObject.addProperty("etatPartie", etatPartie);
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "choisirPlateau": {
				JsonElement idPartieObjet = data.get("idPartie");
				Long idPartie = idPartieObjet.getAsLong();

				JsonElement idPlateauObjet = data.get("idPlateau");
				Long idPlateau = idPlateauObjet.getAsLong();

				// TODO : reception exception - pas d'id dans BD
				Partie partie = playITService.trouverPartieParId(idPartie);
				Plateau plateau = playITService.trouverPlateauParId(idPlateau);
				playITService.choisirPlateau(partie, plateau);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseChoisirPlateau");
				response.addProperty("succes", true);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "lancerActivite": {
				JsonElement idPartieObjet = data.get("idPartie");
				Long idPartie = idPartieObjet.getAsLong();

				// TODO : reception exception - pas d'id dans BD
				Partie partie = playITService.trouverPartieParId(idPartie);
				ActiviteEnCours activiteEnCours = playITService.lancerActivite(partie);

				Activite activite = activiteEnCours.getActivite();

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseLancerActivite");
				response.addProperty("succes", true);

				JsonObject dataObject = new JsonObject();
				if (activite instanceof Question) {
					JsonArray listePropositionsJson = new JsonArray();
					dataObject.addProperty("intitule", ((Question) activite).getIntitule());

					if (activite instanceof QuestionQCM) {
						List<Proposition> listePropositions = ((QuestionQCM) activite).getListePropositions();
						for (Proposition proposition : listePropositions) {
							JsonObject propositionJson = new JsonObject();
							propositionJson.addProperty("intitule", proposition.getIntitule());
							listePropositionsJson.add(propositionJson);
						}
						dataObject.add("listePropositions", listePropositionsJson);
						response.add("data", dataObject);
					}

					if (activite instanceof QuestionVraiFaux) {
						JsonObject propositionVrai = new JsonObject();
						propositionVrai.addProperty("intitule", "Vrai");
						listePropositionsJson.add(propositionVrai);
						JsonObject propositionFaux = new JsonObject();
						propositionFaux.addProperty("intitule", "Faux");
						listePropositionsJson.add(propositionFaux);
						dataObject.add("listePropositions", listePropositionsJson);
						response.add("data", dataObject);
					}

				} else {
					// mini jeu
				}

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "mettreEnPause": {
				JsonElement idPartieObjet = data.get("idPartie");
				Long idPartie = idPartieObjet.getAsLong();

				// TODO : reception exception - pas d'id dans BD
				Partie partie = playITService.trouverPartieParId(idPartie);
				playITService.mettreEnPausePartie(partie);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseMettreEnPausePartie");
				response.addProperty("succes", true);

				String etatPartie = partie.getEtat().toString();
				JsonObject dataObject = new JsonObject();
				dataObject.addProperty("etatPartie", etatPartie);
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "terminerPlateau": {
				JsonElement idPartieObjet = data.get("idPartie");
				Long idPartie = idPartieObjet.getAsLong();

				// TODO : reception exception - pas d'id dans BD
				Partie partie = playITService.trouverPartieParId(idPartie);
				playITService.terminerPartie(partie);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseTerminerPartie");
				response.addProperty("succes", true);

				String etatPartie = partie.getEtat().toString();
				JsonObject dataObject = new JsonObject();
				dataObject.addProperty("etatPartie", etatPartie);
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "listerEquipes": {
				JsonElement idPartieObjet = data.get("idPartie");
				Long idPartie = idPartieObjet.getAsLong();

				// TODO : reception exception - pas d'id dans BD
				Partie partie = playITService.trouverPartieParId(idPartie);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseListerEquipes");
				response.addProperty("succes", true);

				JsonObject dataObject = new JsonObject();
				List<Equipe> listeEquipes = playITService.listerEquipe(partie);
				JsonArray listeEquipesJson = new JsonArray();
				for (Equipe equipe : listeEquipes) {
					JsonObject equipeJson = new JsonObject();
					equipeJson.addProperty("nom", equipe.getNom());
					equipeJson.addProperty("codePin", equipe.getScore());
					
					listeEquipesJson.add(equipeJson);
				}
				dataObject.add("listeEquipes", listeEquipesJson);
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "inscrireEquipe": {
				JsonElement idPartieObjet = data.get("idPartie");
				Long idPartie = idPartieObjet.getAsLong();

				JsonElement nomEquipeObjet = data.get("nomEquipe");
				String nomEquipe = nomEquipeObjet.getAsString();

				// TODO : reception exception - pas d'id dans BD
				Partie partie = playITService.trouverPartieParId(idPartie);
				Equipe equipe = playITService.inscrireEquipe(nomEquipe, partie);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseInscrireEquipe");
				response.addProperty("succes", true);

				JsonObject dataObject = new JsonObject();
				dataObject.addProperty("nomEquipe", equipe.getNom());
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "soumettreReponse": {
				JsonElement idPartieObjet = data.get("idPartie");
				Long idPartie = idPartieObjet.getAsLong();

				JsonElement idPropositionObjet = data.get("idProposition");
				Long idProposition = idPropositionObjet.getAsLong();

				JsonElement idEquipeObjet = data.get("idEquipe");
				Long idEquipe = idEquipeObjet.getAsLong();

				JsonElement idActiviteEnCoursObjet = data.get("idActiviteEnCours");
				Long idActiviteEnCours = idActiviteEnCoursObjet.getAsLong();

				// TODO : reception exception - pas d'id dans BD
				Partie partie = playITService.trouverPartieParId(idPartie);
				Proposition proposition = playITService.trouverPropositionParId(idProposition);
				Equipe equipe = playITService.trouverEquipeParId(idEquipe);
				ActiviteEnCours activiteEnCours = playITService.trouverActiviteEnCoursParId(idActiviteEnCours);
								
				int score = playITService.soumettreReponse(partie, equipe, proposition, activiteEnCours);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseInscrireEquipe");
				response.addProperty("succes", true);

				JsonObject dataObject = new JsonObject();
				dataObject.addProperty("scoreQuestion", score);
				dataObject.addProperty("scoreEquipe", equipe.getScore());
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "modifierEquipe": {

				JsonElement idEquipeObjet = data.get("idEquipe");
				Long idEquipe = idEquipeObjet.getAsLong();

				// TODO : reception exception - pas d'id dans BD
				Equipe equipe = playITService.trouverEquipeParId(idEquipe);

				// TODO : verifier que la partie est bien EN Cours

				JsonElement nouveauNomEquipeObjet = data.get("nouveauNomEquipe");
				String nouveauNomEquipe = nouveauNomEquipeObjet.getAsString();
								
				equipe = playITService.modifierEquipe(equipe, nouveauNomEquipe);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseInscrireEquipe");
				response.addProperty("succes", true);

				JsonObject dataObject = new JsonObject();
				dataObject.addProperty("nouveauNomEquipe", equipe.getNom());
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "validerCodePin": {
				JsonElement codePinObjeet = data.get("codePin");
				String codePin = codePinObjeet.getAsString();

				Partie partie = playITService.validerCodePin(codePin);

				// TODO : vérifier que l'utilisateur est bien authentifié en joueur
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseInscrireEquipe");
				response.addProperty("succes", true);

				JsonObject dataObject = new JsonObject();
				dataObject.addProperty("nomPartie", partie.getNom());
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
		}

		if (controller != null) {
			try {
				controller.handleRequest(session, data, playITService);
			} catch (RoleUtilisateurException e) {
				String reponseType = "reponse" + type.substring(0, 1).toUpperCase() + type.substring(1);

				JsonObject response = new JsonObject();
				response.addProperty("type", reponseType);
				response.addProperty("succes", false);
				response.addProperty("messageErreur", "Vous n'avez pas les droits pour effectuer cette action");
				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);
			} catch (Exception e) {
				// TODO : gérer les autres exceptions
			}
		} else {
			JsonObject response = new JsonObject();
			response.addProperty("type", "erreur");
			response.addProperty("succes", false);
			response.addProperty("messageErreur", "Type de message inconnu : " + type);
			TextMessage responseMessage = new TextMessage(response.toString());
			session.sendMessage(responseMessage);
		}
	}
}