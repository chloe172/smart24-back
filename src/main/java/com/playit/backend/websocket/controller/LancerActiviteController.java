package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.model.Activite;
import com.playit.backend.model.ActiviteEnCours;
import com.playit.backend.model.Partie;
import com.playit.backend.model.Proposition;
import com.playit.backend.model.Question;
import com.playit.backend.model.QuestionQCM;
import com.playit.backend.model.QuestionVraiFaux;
import com.playit.backend.service.PlayITService;
import com.playit.backend.service.NotFoundException;
import com.playit.backend.websocket.handler.SessionRole;

public class LancerActiviteController extends Controller {

    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

        JsonElement idPartieObjet = data.get("idPartie");
        Long idPartie = idPartieObjet.getAsLong();

        JsonObject response = new JsonObject();
        JsonObject dataObject = new JsonObject();
        response.addProperty("type", "reponseLancerActivite");

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

        ActiviteEnCours activiteEnCours;

        try {
            activiteEnCours = playITService.lancerActivite(partie);
        } catch (Exception e) {
            response.addProperty("messageErreur", e.getMessage());
            response.addProperty("succes", false);
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }
        
        Activite activite = activiteEnCours.getActivite();

        response.addProperty("type", "");
        response.addProperty("succes", true);

        if (activite instanceof Question) {
            Question question = (Question) activite;
            JsonArray listePropositionsJson = new JsonArray();
            dataObject.addProperty("intitule", question.getIntitule());

            if (question instanceof QuestionQCM) {
                List<Proposition> listePropositions = ((QuestionQCM) activite).getListePropositions();
                for (Proposition proposition : listePropositions) {
                    JsonObject propositionJson = new JsonObject();
                    propositionJson.addProperty("intitule", proposition.getIntitule());
                    listePropositionsJson.add(propositionJson);
                }
                dataObject.add("listePropositions", listePropositionsJson);
                response.add("data", dataObject);
            } else if (question instanceof QuestionVraiFaux) {
                JsonObject propositionVrai = new JsonObject();
                propositionVrai.addProperty("intitule", "Vrai");
                listePropositionsJson.add(propositionVrai);
                JsonObject propositionFaux = new JsonObject();
                propositionFaux.addProperty("intitule", "Faux");
                listePropositionsJson.add(propositionFaux);
                dataObject.add("listePropositions", listePropositionsJson);
                response.add("data", dataObject);
            }

            // TODO : envoyer la question à tous les joueurs

            // TODO : lancer un timer sur la durée de la question pour envoyer la réponse après/mettre fin aux réponses
            // et passer la partie en état EXPLICATION
        } else {
            // mini jeu
        }

        TextMessage responseMessage = new TextMessage(response.toString());
        session.sendMessage(responseMessage);

        return;

    }
    
}
