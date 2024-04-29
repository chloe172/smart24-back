package com.playit.backend;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.playit.backend.model.Question;
import com.playit.backend.repository.QuestionRepository;
import com.playit.backend.service.PlayITService;

@SpringBootApplication
@RestController
public class PlayItBackendApplication {
	@Autowired
	QuestionRepository questionRepository;

	@Autowired
	PlayITService playITService;

	public static void main(String[] args) {
		SpringApplication.run(PlayItBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(QuestionRepository questionRepository) {
		return args -> {
			questionRepository.save(new Question("Quelle est la capitale de la France ?"));
			questionRepository.save(new Question("Quelle est la capitale de l'Espagne ?"));
			questionRepository.save(new Question("Quelle est la capitale de l'Allemagne ?"));
			questionRepository.flush();
			System.out.println("Questions ajoutées à la base de données");
		};
	}

	@GetMapping("/")
	public String index() {
		List<Question> questions = this.questionRepository.findAll();
		String result = "";
		for (Question question : questions) {
			result += question.getIntitule() + "<br>";
		}
		return result;
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

	@GetMapping("/createQuestion")
	public String createQuestion(@RequestParam(value = "question") String question) {
		this.questionRepository.saveAndFlush(new Question(question));

		return "Question ajoutée";
	}
}