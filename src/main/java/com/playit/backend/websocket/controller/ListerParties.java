package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.playit.backend.model.MaitreDuJeu;
import com.playit.backend.model.Partie;
import com.playit.backend.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;

public class ListerParties extends Controller {

    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

        MaitreDuJeu maitreDuJeu;
        Long idMaitreDuJeu = (Long) session.getAttributes().get("idMaitreDuJeu");
        System.out.println("idMaitreDuJeu: " + idMaitreDuJeu);

        JsonObject response = new JsonObject();
        JsonObject dataObject = new JsonObject();
        response.addProperty("type", "reponseListerParties");

        try {
            maitreDuJeu = playITService.trouverMaitreDuJeuParId(idMaitreDuJeu);
        } catch (Exception e) {
            response.addProperty("messageErreur", "Maitre du jeu non trouvé");
            response.addProperty("succes", false);
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }
        
        response.addProperty("succes", true);

        List<Partie> listeParties = playITService.listerParties(maitreDuJeu);
        JsonArray listePartiesJson = new JsonArray();
        for (Partie partie : listeParties) {
            JsonObject partieJson = new JsonObject();
            partieJson.addProperty("nom", partie.getNom());
            partieJson.addProperty("codePin", partie.getCodePin());
            partieJson.addProperty("etat", partie.getEtat().toString());
            partieJson.addProperty("date", partie.getDate().toString());
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
    
}
