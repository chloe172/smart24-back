package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.model.Partie;
import com.playit.backend.model.Plateau;
import com.playit.backend.service.PlayITService;
import com.playit.backend.service.NotFoundException;
import com.playit.backend.websocket.handler.SessionRole;

public class ChoisirPlateauController extends Controller {
    
    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

        JsonElement idPartieObjet = data.get("idPartie");
        Long idPartie = idPartieObjet.getAsLong();

        JsonElement idPlateauObjet = data.get("idPlateau");
        Long idPlateau = idPlateauObjet.getAsLong();

        JsonObject response = new JsonObject();
        JsonObject dataObject = new JsonObject();
        response.addProperty("type", "reponseChoisirPlateau");

        Partie partie;
        try {
            partie = playITService.trouverPartieParId(idPartie);
        } catch (NotFoundException e) {
            response.addProperty("messageErreur", "Partie non trouvée");
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
            response.addProperty("succes", false);
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }

        try {
            playITService.choisirPlateau(partie, plateau);
        } catch (IllegalArgumentException e) {
            response.addProperty("messageErreur", e.getMessage());
            response.addProperty("succes", false);
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }

        response.addProperty("succes", true);
        response.add("data", dataObject);

        TextMessage responseMessage = new TextMessage(response.toString());
        session.sendMessage(responseMessage);

        return;
    }
}
