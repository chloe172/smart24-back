package com.playit.backend.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.playit.backend.websocket.handler.PlayITHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Autowired
	private PlayITHandler playITHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		WebSocketHandler handler = playITHandler;
		String socketUrl = "/socket";

		registry.addHandler(handler, socketUrl)
				.setAllowedOrigins("*")
				.withSockJS();
		registry.addHandler(handler, socketUrl)
				.setAllowedOrigins("*");

	}

}
