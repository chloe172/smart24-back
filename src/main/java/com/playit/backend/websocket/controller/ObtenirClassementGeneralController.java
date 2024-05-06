package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;

public class ObtenirClassementGeneralController extends Controller {

    @Override
    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

        Long idPartie = data.get("idPartie").getAsLong();

        JsonObject response = new JsonObject();
        response.addProperty("type", "reponseObtenirClassementGeneral");
        response.addProperty("succes", true);

        Partie partie = null;
        try {
            partie = playITService.trouverPartieParId(idPartie);
        } catch (NotFoundException e) {
            response.addProperty("succes", false);
            response.addProperty("codeErreur", 404);
            response.addProperty("messageErreur", "Partie non trouvée");
            session.sendMessage(new TextMessage(response.toString()));
            return;
        }

        List<Equipe> classement = playITService.obtenirEquipesParRang(partie);

        JsonArray classementArray = new JsonArray();
        int i = 1;
        for (Equipe equipe : classement) {
            JsonObject equipeObject = new JsonObject();
            equipeObject.addProperty("id", equipe.getId());
            equipeObject.addProperty("nom", equipe.getNom());
            equipeObject.addProperty("avatar", equipe.getAvatar().toString());
            equipeObject.addProperty("score", equipe.getScore());
            if (i == 1) {
                equipeObject.addProperty("rang", "1er");
            } else {
                equipeObject.addProperty("rang", i + "ème");
            }
            classementArray.add(equipeObject);
            i++;
        }
        JsonObject dataObject = new JsonObject();
        dataObject.add("classement", classementArray);
        response.add("data", dataObject);

        TextMessage responseMessage = new TextMessage(response.toString());
        session.sendMessage(responseMessage);

    }

}
