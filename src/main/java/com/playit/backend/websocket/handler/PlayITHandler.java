package com.playit.backend.websocket.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
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
import com.playit.backend.service.PlayITService;

@Service // TODO : Vérifier que c'est le bon type d'annotation
public class PlayITHandler extends TextWebSocketHandler {

	@Autowired
	PlayITService playITService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws IOException {
		System.out.println("New session: " + session.getId());
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

		switch (type) {
			case "authentifierUtilisateur": {
				JsonElement nomObjet = data.get("nom");
				String nom = nomObjet.getAsString();
				JsonElement motDePasseObjet = data.get("mdp");
				String motDePasse = motDePasseObjet.getAsString();

				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseAuthentifierUtilisateur");
				try {
					MaitreDuJeu maitreDuJeu = playITService.authentifier(nom, motDePasse);
					// TODO : associer la session au maitre du jeu
					response.addProperty("succes", true);
				} catch (IllegalArgumentException e) {
					response.addProperty("succes", false);
					response.addProperty("messageErreur", "Nom ou mot de passe incorrect");
				}
				TextMessage responseMessage = new TextMessage(response.toString());
				System.out.println("Message sent: " + responseMessage.getPayload());
				session.sendMessage(responseMessage);

				return;
			}
			case "listerPlateaux": {
				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseListerPlateaux");

				JsonObject dataObject = new JsonObject();
				List<Plateau> listePlateaux = playITService.listerPlateaux();
				JsonArray listePlateauxJson = new JsonArray();
				for (Plateau plateau : listePlateaux) {
					JsonObject plateauJson = new JsonObject();
					plateauJson.addProperty("nom", plateau.getNom());
					listePlateauxJson.add(plateauJson);
				}
				dataObject.add("listePlateaux", listePlateauxJson);
				response.add("data", dataObject);
				response.addProperty("succes", true);
				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "listerPlateauxPartie": {
				JsonElement idObjet = data.get("idPartie");
				Long idPartie = idObjet.getAsLong();

				// TODO : reception exception
				Partie partie = playITService.trouverPartieParId(idPartie);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseListerPlateaux");

				JsonObject dataObject = new JsonObject();
				List<Plateau> listePlateaux = playITService.listerPlateauxDansPartie(partie);
				JsonArray listePlateauxJson = new JsonArray();
				for (Plateau plateau : listePlateaux) {
					JsonObject plateauJson = new JsonObject();
					plateauJson.addProperty("nom", plateau.getNom());
					listePlateauxJson.add(plateauJson);
				}
				dataObject.add("listePlateaux", listePlateauxJson);
				response.add("data", dataObject);
				response.addProperty("succes", true);
				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "creerPartie": {
				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonElement idMaitreDuJeuObjet = data.get("idMaitreDuJeu");
				Long idMaitreDuJeu = idMaitreDuJeuObjet.getAsLong();
				MaitreDuJeu maitreDuJeu = playITService.trouverMaitreDuJeuParId(idMaitreDuJeu);

				JsonElement nomPartieObjet = data.get("nomPartie");
				String nomPartie = nomPartieObjet.getAsString();

				JsonArray listePlateauxJson = data.get("plateaux").getAsJsonArray();
				List<Plateau> listePlateaux = new ArrayList<>();

				for (JsonElement plateauJson : listePlateauxJson) {
					Long idPlateau = plateauJson.getAsLong();
					Plateau plateau = playITService.trouverPlateauParId(idPlateau);
					listePlateaux.add(plateau);
				}

				Partie partie = playITService.creerPartie(nomPartie, maitreDuJeu, listePlateaux);

				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseCreerPartie");
				response.addProperty("succes", true);

				JsonObject dataObject = new JsonObject();
				dataObject.addProperty("idPartie", partie.getId());
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
			}
			case "listerParties": {
				JsonElement idMaitreDuJeuObjet = data.get("idMaitreDuJeu");
				Long idMaitreDuJeu = idMaitreDuJeuObjet.getAsLong();

				// TODO : reception exception - pas d'id dans BD
				MaitreDuJeu maitreDuJeu = playITService.trouverMaitreDuJeuParId(idMaitreDuJeu);

				// TODO : vérifier que l'utilisateur est bien authentifié en maitre du jeu
				JsonObject response = new JsonObject();
				response.addProperty("type", "reponseListerParties");
				response.addProperty("succes", true);

				JsonObject dataObject = new JsonObject();
				List<Partie> listeParties = playITService.listerParties(maitreDuJeu);
				JsonArray listePartiesJson = new JsonArray();
				for (Partie partie : listeParties) {
					JsonObject partieJson = new JsonObject();
					partieJson.addProperty("nom", partie.getNom());
					partieJson.addProperty("codePin", partie.getCodePin());
					partieJson.addProperty("etat", partie.getEtat().toString());
					if (partie.getPlateauCourant() != null) {
						partieJson.addProperty("dernierPlateau", partie.getPlateauCourant().getNom());
					} else {
						partieJson.add("dernierPlateau", JsonNull.INSTANCE);
					}

					listePartiesJson.add(partieJson);
				}
				dataObject.add("listeParties", listePartiesJson);
				response.add("data", dataObject);

				TextMessage responseMessage = new TextMessage(response.toString());
				session.sendMessage(responseMessage);

				return;
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
				response.addProperty("type", "reponseDemarrerPartie");
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
		}
	}
}