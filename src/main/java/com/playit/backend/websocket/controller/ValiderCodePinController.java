package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.model.Partie;
import com.playit.backend.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class ValiderCodePinController extends Controller {

    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.ANONYME);  

        String codePin = data.get("codePin").getAsString();

        Partie partie = null;
        try {
            partie = playITService.validerCodePin(codePin);
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "reponseValiderCodePin");
            response.addProperty("succes", false);
            response.addProperty("messageErreur", e.getMessage());
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }

        JsonObject response = new JsonObject();
        response.addProperty("type", "reponseValiderCodePin");
        response.addProperty("succes", true);

        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("idPartie", partie.getId());
        dataObject.addProperty("nomPartie", partie.getNom());
        dataObject.addProperty("etatPartie", partie.getEtat().toString());
        response.add("data", dataObject);

        AssociationSessionsParties.ajouterSessionEquipeAPartie(session, partie);

        TextMessage responseMessage = new TextMessage(response.toString());
        session.sendMessage(responseMessage);

        return;

    }
    
}
