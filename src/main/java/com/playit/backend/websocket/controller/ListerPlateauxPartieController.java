package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.model.Partie;
import com.playit.backend.model.Plateau;
import com.playit.backend.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;

public class ListerPlateauxPartieController extends Controller {

    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        
        this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

        JsonElement idObjet = data.get("idPartie");
        Long idPartie = idObjet.getAsLong();
        
        JsonObject response = new JsonObject();
        JsonObject dataObject = new JsonObject();
        response.addProperty("type", "reponseListerPlateaux");

        Partie partie;
        
        try {
            partie = playITService.trouverPartieParId(idPartie);
        } catch (Exception e) {
            response.addProperty("messageErreur", "Partie non trouvée");
            response.addProperty("succes", false);
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }

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
    
}
