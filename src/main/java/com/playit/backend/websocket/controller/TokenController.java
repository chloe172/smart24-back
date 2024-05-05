package com.playit.backend.websocket.controller;

import java.util.Map;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.websocket.handler.SessionRole;
import com.playit.backend.websocket.handler.TokenSessionAssociation;

public class TokenController extends Controller {

    @Override
    public void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception {
        this.userHasRoleOrThrow(session, SessionRole.ANONYME);

        boolean asking = data.get("asking").getAsBoolean();
        if (asking) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "reponseToken");
            response.addProperty("succes", true);

            String token = TokenSessionAssociation.addSession(session);
            System.out.println(token);

            JsonObject dataResponse = new JsonObject();
            dataResponse.addProperty("token", token);
            response.add("data", dataResponse);
            TextMessage message = new TextMessage(response.toString());
            session.sendMessage(message);

        } else {
            String token = data.get("token").getAsString();
            WebSocketSession associatedSession = TokenSessionAssociation.getSession(token);
            if (associatedSession == null) {
                TokenSessionAssociation.addSession(session);
                TokenSessionAssociation.addSession(associatedSession);
                JsonObject response = new JsonObject();
                response.addProperty("type", "reponseToken");
                response.addProperty("succes", false);
                JsonObject dataResponse = new JsonObject();
                dataResponse.addProperty("messageErreur", "Token invalide");
                response.add("data", dataResponse);
                TextMessage message = new TextMessage(response.toString());
                session.sendMessage(message);

            } else { // une session est déjà associée à ce token
                for (Map.Entry<String, Object> entry : associatedSession.getAttributes().entrySet()) {
                    session.getAttributes().put(entry.getKey(), entry.getValue());
                }

                JsonObject response = new JsonObject();
                response.addProperty("type", "reponseToken");
                response.addProperty("succes", true);
                JsonObject dataResponse = new JsonObject();
                SessionRole role = (SessionRole) associatedSession.getAttributes().get("role");
                dataResponse.addProperty("role", role.toString());
                response.add("data", dataResponse);
                TextMessage message = new TextMessage(response.toString());
                session.sendMessage(message);
            }
        }
    }

}
