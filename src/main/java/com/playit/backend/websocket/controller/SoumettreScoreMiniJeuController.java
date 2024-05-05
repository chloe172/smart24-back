package com.playit.backend.websocket.controller;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.metier.model.ActiviteEnCours;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.service.NotFoundException;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.AssociationSessionsParties;
import com.playit.backend.websocket.handler.SessionRole;

public class SoumettreScoreMiniJeuController extends Controller {

    @Override
    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.EQUIPE);

        int score = data.get("score").getAsInt();
        Long idActiviteEnCours = data.get("idActiviteEnCours").getAsLong();

        Long idEquipe = (Long) session.getAttributes().get("idEquipe");

        Equipe equipe = playITService.trouverEquipeParId(idEquipe);
        ActiviteEnCours activiteEnCours = null;
        Partie partie = equipe.getPartie();

        JsonObject response = new JsonObject();
        response.addProperty("type", "reponseSoumettreScoreMinijeu");
        response.addProperty("succes", true);
        try {
            activiteEnCours = playITService.trouverActiviteEnCoursParId(idActiviteEnCours);
        } catch (NotFoundException e) {
            response.addProperty("succes", false);
            response.addProperty("messageErreur", "Activité non trouvée");
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }

        try {
            playITService.soumettreScoreMiniJeu(partie, activiteEnCours, equipe, score);
        } catch (IllegalStateException e) {
            response.addProperty("succes", false);
            response.addProperty("messageErreur", e.getMessage());
            TextMessage responseMessage = new TextMessage(response.toString());
            session.sendMessage(responseMessage);
            return;
        }

        JsonObject dataObject = new JsonObject();
        response.add("data", dataObject);
        TextMessage responseMessage = new TextMessage(response.toString());
        session.sendMessage(responseMessage);

        JsonObject notification = new JsonObject();
        notification.addProperty("type", "notificationSoumettreScoreMinijeu");
        notification.addProperty("succes", true);
        JsonObject dataNotification = new JsonObject();
        JsonObject equipeObject = new JsonObject();
        equipeObject.addProperty("id", equipe.getId());
        equipeObject.addProperty("nom", equipe.getNom());
        equipeObject.addProperty("score", score);
        equipeObject.addProperty("avatar", equipe.getAvatar().toString());
        dataNotification.add("equipe", equipeObject);
        notification.add("data", dataNotification);
        TextMessage notificationMessage = new TextMessage(notification.toString());
        AssociationSessionsParties.getMaitreDuJeuPartie(partie).sendMessage(notificationMessage);
    }

}
