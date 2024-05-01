package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.model.Equipe;
import com.playit.backend.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;

public class ModifierEquipeController extends Controller {

    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.EQUIPE);  
        
        JsonElement idEquipeObjet = data.get("idEquipe");
        Long idEquipe = idEquipeObjet.getAsLong();

        Equipe equipe = null;
        try {
            equipe = playITService.trouverEquipeParId(idEquipe);
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "reponseModifierEquipe");
            response.addProperty("succes", false);
            response.addProperty("messageErreur", "Equipe non trouvée");
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }

        JsonElement nouveauNomEquipeObjet = data.get("nouveauNomEquipe");
        String nouveauNomEquipe = nouveauNomEquipeObjet.getAsString();
        
        try {
            equipe = playITService.modifierEquipe(equipe, nouveauNomEquipe);
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "reponseModifierEquipe");
            response.addProperty("succes", false);
            // TODO : verifier que la partie est bien EN Cours + nom equipe OK + pas d'erreur de commit dans BD
            response.addProperty("messageErreur", "?????");
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }
        
        // TODO : envoyer une notif au maitre du jeu pour changement nom equipe
        JsonObject response = new JsonObject();
        response.addProperty("type", "reponseModifierEquipe");
        response.addProperty("succes", true);

        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("nouveauNomEquipe", equipe.getNom());
        response.add("data", dataObject);

        TextMessage responseMessage = new TextMessage(response.toString());
        session.sendMessage(responseMessage);

        return;
    }
}
