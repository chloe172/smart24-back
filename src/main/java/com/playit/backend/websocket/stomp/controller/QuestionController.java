package com.playit.backend.websocket.stomp.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class QuestionController {

	@MessageMapping("/post-question")
	@SendTo("/topic/get-question")
	public String uploadQuestion(@Payload String question) {
		System.out.println("Received question: " + question);
		return question;
	}

	@MessageMapping("/post-answer")
	@SendTo("/topic/get-answer")
	public String gatherAnswer(@Payload String answer) {
		System.out.println("Received answer: " + answer);
		return answer;
	}

}
