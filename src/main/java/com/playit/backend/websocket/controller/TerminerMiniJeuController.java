package com.playit.backend.websocket.controller;

import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.model.ScorePlateau;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class TerminerMiniJeuController extends Controller {

    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.MAITRE_DU_JEU);

        JsonElement idPartieObjet = data.get("idPartie");
        Long idPartie = idPartieObjet.getAsLong();

        JsonObject response = new JsonObject();
        response.addProperty("type", "reponseTerminerMinijeu");
        response.addProperty("succes", true);

        Partie partie;
        try {
            partie = playITService.trouverPartieParId(idPartie);
        } catch (NotFoundException e) {
            response.addProperty("messageErreur", "Partie non trouvée");
            response.addProperty("codeErreur", 404);
            response.addProperty("succes", false);
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }

        TextMessage reponseTerminerMessage = new TextMessage(response.toString());
        try {
            session.sendMessage(reponseTerminerMessage);
        } catch (Exception e) {
        }

        response.addProperty("type", "notificationReponseActivite");
        List<WebSocketSession> listeSocketSessionsEquipes = AssociationSessionsParties.getEquipesParPartie(partie);
        JsonObject dataObject = new JsonObject();

        playITService.passerEnModeExplication(partie);
        Plateau plateau = partie.getPlateauCourant().getPlateau();
        List<ScorePlateau> listeScore = playITService.obtenirEquipesParRang(partie, plateau);
        dataObject.addProperty("nomPlateauCourant", plateau.getNom());
        dataObject.addProperty("typeActivite", "finMinijeu");

        for (WebSocketSession sessionEquipe : listeSocketSessionsEquipes) {
            Long idEquipe = (Long) sessionEquipe.getAttributes().get("idEquipe");
            Equipe equipe = null;
            try {
                equipe = playITService.trouverEquipeParId(idEquipe);
            } catch (NotFoundException e) {
                continue;
            }
            JsonObject equipeJson = new JsonObject();
            equipeJson.addProperty("id", equipe.getId());
            equipeJson.addProperty("nom", equipe.getNom());
            final Equipe finalEquipe = equipe; // Pour fix un problème de portée askip
            int score = listeScore.stream()
                    .filter(scorePlateau -> scorePlateau.getEquipe().getId().equals(finalEquipe.getId()))
                    .findFirst()
                    .map(ScorePlateau::getScore)
                    .orElse(0);
            equipeJson.addProperty("score", score);
            dataObject.add("equipe", equipeJson);
            response.add("data", dataObject);
            TextMessage miniJeuMessage = new TextMessage(response.toString());
            try {
                sessionEquipe.sendMessage(miniJeuMessage);
            } catch (Exception e) {
            }
        }

        // Envoi au maitre du jeu
        JsonArray listeEquipesJson = new JsonArray();
        List<Equipe> listeEquipes = playITService.obtenirEquipesParRang(partie);
        for (int i = 0; i < listeEquipes.size(); i++) {
            Equipe equipe = listeEquipes.get(i);
            JsonObject equipeJson = new JsonObject();
            equipeJson.addProperty("id", equipe.getId());
            equipeJson.addProperty("nom", equipe.getNom());
            int score = listeScore.stream()
                    .filter(scorePlateau -> scorePlateau.getEquipe().getId().equals(equipe.getId()))
                    .findFirst()
                    .map(ScorePlateau::getScore)
                    .orElse(0);
            equipeJson.addProperty("score", score);
            equipeJson.addProperty("avatar", equipe.getAvatar().toString());
            if (i == 0) {
                equipeJson.addProperty("rang", "1er");
            } else {
                equipeJson.addProperty("rang", i + 1 + "ème");
            }

            listeEquipesJson.add(equipeJson);
        }
        dataObject.add("listeEquipes", listeEquipesJson);
        response.add("data", dataObject);

        TextMessage miniJeuMessage = new TextMessage(response.toString());
        try {
            session.sendMessage(miniJeuMessage);
        } catch (Exception e) {
        }

    }

}
