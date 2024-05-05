package com.playit.backend.websocket.controller;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.metier.model.MaitreDuJeu;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;

public class AuthentifierUtilisateurController extends Controller {

	@Override
	public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService)
			throws IOException {

		String nom = data.get("nom")
				.getAsString();
		String motDePasse = data.get("mdp")
				.getAsString();

		JsonObject response = new JsonObject();
		response.addProperty("type", "reponseAuthentifierUtilisateur");
		response.addProperty("succes", true);

		try {
			MaitreDuJeu maitreDuJeu = playITService.authentifier(nom, motDePasse);
			session.getAttributes()
					.put("role", SessionRole.MAITRE_DU_JEU);
			session.getAttributes()
					.put("idMaitreDuJeu", maitreDuJeu.getId());
		} catch (IllegalArgumentException e) {
			response.addProperty("succes", false);
			response.addProperty("codeErreur", 422);
			response.addProperty("messageErreur", "Nom ou mot de passe incorrect");
		}

		JsonObject dataObject = new JsonObject();
		response.add("data", dataObject);

		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);

		return;
	}

}