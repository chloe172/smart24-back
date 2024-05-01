package com.playit.backend.websocket.controller;

import com.google.gson.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import com.playit.backend.service.PlayITService;
import com.playit.backend.websocket.RoleUtilisateurException;
import com.playit.backend.websocket.handler.SessionRole;

public abstract class Controller {

	@Autowired
	PlayITService playITService;

	public abstract void handleRequest(WebSocketSession session, JsonObject data, PlayITService playITService) throws Exception;

	protected void userHasRoleOrThrow(WebSocketSession session, SessionRole role) throws RoleUtilisateurException {
		SessionRole userRole = (SessionRole) session.getAttributes().get("role");
		if (userRole != role) {
			throw new RoleUtilisateurException("L'utilisateur n'a pas le rôle nécessaire pour effectuer cette action.");
		}
	}

}