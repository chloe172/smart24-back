package com.playit.backend.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.playit.backend.websocket.handler.QuestionAnswerHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		WebSocketHandler handler = new QuestionAnswerHandler();

		registry.addHandler(handler, "/socket-classic")
				.setAllowedOrigins("*")
				.withSockJS();
		registry.addHandler(handler, "/socket-classic")
				.setAllowedOrigins("*");
	}

}
