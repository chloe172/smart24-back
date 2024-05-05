package com.playit.backend.websocket.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.socket.WebSocketSession;

public class TokenSessionAssociation {

    private static Map<String, WebSocketSession> tokenSessionMap = new HashMap<>();

    public static String addSession(WebSocketSession session) {
        for (Map.Entry<String, WebSocketSession> entry : tokenSessionMap.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        String token = generateToken();
        tokenSessionMap.put(token, session);
        return token;
    }

    public static WebSocketSession getSession(String token) {
        return tokenSessionMap.get(token);
    }

    private static String generateToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String tokenPart = generateTokenPart();
            token.append(tokenPart);
        }
        return token.toString();
    }

    private static String generateTokenPart() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

}
