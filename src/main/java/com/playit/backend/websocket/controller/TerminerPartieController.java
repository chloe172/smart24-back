package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.model.Partie;
import com.playit.backend.service.NotFoundException;
import com.playit.backend.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;

public class TerminerPartieController extends Controller {

    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

        JsonElement idPartieObjet = data.get("idPartie");
				Long idPartie = idPartieObjet.getAsLong();

				Partie partie = null;
				try {
					partie = playITService.trouverPartieParId(idPartie);
				} catch (NotFoundException e) {
					JsonObject response = new JsonObject();
					response.addProperty("type", "reponseTerminerPartie");
					response.addProperty("succes", false);
					response.addProperty("messageErreur", "Partie non trouvée");
					TextMessage responseMessage = new TextMessage(response.toString());
					session.sendMessage(responseMessage);
					return;
				}

                try {
                    playITService.terminerPartie(partie);
                } catch (Exception e) {
                    JsonObject response = new JsonObject();
					response.addProperty("type", "reponseTerminerPartie");
					response.addProperty("succes", false);
					response.addProperty("messageErreur", "Etat Partie non adapté");
					TextMessage responseMessage = new TextMessage(response.toString());
					session.sendMessage(responseMessage);
					return;
                }
				
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
    
}
