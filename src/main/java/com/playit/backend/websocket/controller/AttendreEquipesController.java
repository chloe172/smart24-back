package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.model.Partie;
import com.playit.backend.service.PlayITService;
import com.playit.backend.service.NotFoundException;
import com.playit.backend.websocket.handler.SessionRole;

public class AttendreEquipesController extends Controller {

    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

        Long idPartie = data.get("idPartie").getAsLong();

        JsonObject response = new JsonObject();
        JsonObject dataObject = new JsonObject();
        response.addProperty("type", "reponseAttendreEquipes");

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
        
        try {
            playITService.attendreEquipes(partie);
        } catch (IllegalStateException e) {
            response.addProperty("messageErreur", e.getMessage());
            response.addProperty("succes", false);
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }

        response.addProperty("type", "reponseAttendreEquipes");
        response.addProperty("succes", true);

        String etatPartie = partie.getEtat().toString();
        dataObject.addProperty("etatPartie", etatPartie);
        dataObject.addProperty("codePin", partie.getCodePin());
        response.add("data", dataObject);

        TextMessage responseMessage = new TextMessage(response.toString());
        session.sendMessage(responseMessage);

        return;
    }
    
}
