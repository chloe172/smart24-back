package com.playit.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.playit.backend.metier.model.MaitreDuJeu;
import com.playit.backend.repository.MaitreDuJeuRepository;

@Configuration
public class PlayITConfig {

	@Bean
	CommandLineRunner creationJeuDeDonnees(MaitreDuJeuRepository maitreDuJeuRepository) {
		return args -> {
			maitreDuJeuRepository.save(new MaitreDuJeu("admin@volvo.fr", "1234"));
			maitreDuJeuRepository.save(new MaitreDuJeu("admin1@volvo.fr", "1234"));
			maitreDuJeuRepository.save(new MaitreDuJeu("admin3@volvo.fr", "1234"));
			maitreDuJeuRepository.flush();
			System.out.println("Maitres du jeu ajoutés");
		};

	}

}