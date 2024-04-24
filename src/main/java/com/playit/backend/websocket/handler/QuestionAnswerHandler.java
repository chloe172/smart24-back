package com.playit.backend.websocket.handler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestionAnswerHandler extends TextWebSocketHandler {
	private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
	private final List<WebSocketSession> teamSessions = new CopyOnWriteArrayList<>();
	private WebSocketSession hostSession;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws IOException {
		this.sessions.add(session);
		this.sendSuccessMessage("Connection established", session);

		System.out.println("New session: " + session.getId());
		System.out.println(this.sessions.size() + " sessions connected");
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		if (session == this.hostSession) {
			this.hostSession = null;
		} else {
			this.teamSessions.remove(session);
		}
		this.sessions.remove(session);

		System.out.println("Session closed: " + session.getId());
		System.out.println(this.sessions.size() + " sessions connected");
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
		System.out.println("Message received from " + session.getId());
		JsonObject body = new Gson().fromJson(message.getPayload(), JsonObject.class);
		JsonElement typeElement = body.get("type");
		if (typeElement == null) {
			this.sendErrorMessage("Type not found", session);
			return;
		}

		String type = typeElement.getAsString();

		System.out.println("Type: " + type);
		switch (type) {
		case "register": {
			this.handleRegistration(session, body);
			return;
		}

		case "question": {
			this.handleQuestion(session, body);
			return;
		}

		case "answer": {
			this.handleAnswer(session, body);
			return;
		}

		default:
			this.sendErrorMessage("Invalid type", session);
		}

	}

	private void handleRegistration(WebSocketSession session, JsonObject body) throws IOException {
		JsonElement roleElement = body.get("role");
		String role = roleElement.getAsString();

		switch (role) {
		case "host": {
			if (this.hostSession != null) {
				this.sendErrorMessage("The game already has an host", session);
				return;
			}

			if (this.teamSessions.contains(session)) {
				this.sendErrorMessage("Cannot be team and host at the same time", session);
				return;
			}

			this.hostSession = session;
			this.sendSuccessMessage("Host registered", session);

			return;
		}
		case "team": {
			if (session == this.hostSession) {
				this.sendErrorMessage("Cannot be host and team at the same time", session);
				return;
			}

			if (this.teamSessions.contains(session)) {
				this.sendErrorMessage("Cannot join the game twice", session);
				return;
			}

			this.teamSessions.add(session);
			this.sendSuccessMessage("Team registered", session);

			return;
		}
		default: {
			this.sendErrorMessage("Invalid role", session);
		}
		}
	}

	private void handleQuestion(WebSocketSession session, JsonObject body) throws IOException {
		// TODO : check if the session is the host
		JsonElement questionElement = body.get("question");
		String question = questionElement.getAsString();

		JsonObject response = new JsonObject();
		response.addProperty("type", "question");
		response.addProperty("question", question);
		TextMessage responseMessage = new TextMessage(response.toString());

		for (WebSocketSession team : this.teamSessions) {
			try {
				team.sendMessage(responseMessage);
			} catch (IOException e) {

			}
		}

		this.sendSuccessMessage("Question sent", session);

	}

	private void handleAnswer(WebSocketSession session, JsonObject body) throws IOException {
		// TODO : check if the session is a team
		JsonElement answerElement = body.get("answer");
		String answer = answerElement.getAsString();

		if (this.hostSession != null) {
			JsonObject response = new JsonObject();
			response.addProperty("type", "answer");
			response.addProperty("answer", answer);
			TextMessage responseMessage = new TextMessage(response.toString());

			this.hostSession.sendMessage(responseMessage);
			this.sendSuccessMessage("Answer sent", session);
		} else {
			this.sendErrorMessage("No host connected", session);
		}
	}

	// Helper methods
	private void sendSuccessMessage(String message, WebSocketSession session) throws IOException {
		JsonObject response = new JsonObject();
		response.addProperty("message", message);
		response.addProperty("success", true);
		TextMessage responseMessage = new TextMessage(response.toString());
		session.sendMessage(responseMessage);
	}

	private void sendErrorMessage(String message, WebSocketSession session) throws IOException {
		JsonObject error = new JsonObject();
		error.addProperty("error", message);
		TextMessage errorMessage = new TextMessage(error.toString());
		session.sendMessage(errorMessage);
	}

}
