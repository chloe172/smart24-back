package com.playit.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.playit.backend.metier.model.MaitreDuJeu;
import com.playit.backend.repository.MaitreDuJeuRepository;

@Configuration
public class PlayITConfig {
	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner creationJeuDeDonnees(MaitreDuJeuRepository maitreDuJeuRepository) {
		return args -> {
			String motDePasseEncode = encoder().encode("1234");
			maitreDuJeuRepository.save(new MaitreDuJeu("admin@volvo.fr", motDePasseEncode));
			maitreDuJeuRepository.save(new MaitreDuJeu("admin2@volvo.fr", motDePasseEncode));
			maitreDuJeuRepository.save(new MaitreDuJeu("admin3@volvo.fr", motDePasseEncode));
			maitreDuJeuRepository.flush();
			System.out.println("Maitres du jeu ajoutés");
		};

	}

}