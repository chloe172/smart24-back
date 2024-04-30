package com.playit.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.playit.backend.model.MaitreDuJeu;
import com.playit.backend.model.Partie;
import com.playit.backend.model.Plateau;
import com.playit.backend.model.QuestionVraiFaux;
import com.playit.backend.model.QuestionQCM;
import com.playit.backend.model.ActiviteEnCours;
import com.playit.backend.model.DifficulteActivite;
import com.playit.backend.model.Proposition;
import com.playit.backend.repository.MaitreDuJeuRepository;
import com.playit.backend.repository.PlateauRepository;
import com.playit.backend.repository.QuestionRepository;
import com.playit.backend.service.PlayITService;

import java.util.List;

@Configuration
public class PlayITConfig {

	@Bean
	CommandLineRunner creationMaitreDuJeu(MaitreDuJeuRepository maitreDuJeuRepository) {
		return args -> {
			maitreDuJeuRepository.save(new MaitreDuJeu("admin@volvo.fr", "1234"));
			maitreDuJeuRepository.flush();
			System.out.println("Maitre du jeu ajouté");
		};
	}

	@Bean
	CommandLineRunner creationPlateau(QuestionRepository questionRepository, PlateauRepository plateauRepository,
			MaitreDuJeuRepository maitreDuJeuRepository, PlayITService playITService) {
		return args -> {
			Plateau plateauGeneral = new Plateau("Général");
			Plateau plateauCyber = new Plateau("Cyber");
			QuestionVraiFaux question1 = new QuestionVraiFaux(DifficulteActivite.MOYEN,
					"L'informatique est apparu avec les ordinateurs ?",
					0,
					"Informatique = jonction de plusieurs concepts : les algorithmes, les langages, les machines et les informations. Origines du concept très vieilles (un des plus vieux du monde : les scribes d’Egypte et de Mésopotamie en utilisaient déjà à leur époque). Ordinateurs sont eux apparus au milieu du XXe siècle pendant la seconde guerre mondiale par les anglais pour déchiffrer le code Enigma. (Il existait des programmes informatiques bien avant le premier ordinateur!)",
					new Proposition("Faux"));
			QuestionVraiFaux question2 = new QuestionVraiFaux(DifficulteActivite.MOYEN,
					"Le terme bug vient de la présence d'insectes dans les ordinateurs ?", 1, ",sdn:fljlkdmjfl",
					new Proposition("Vrai"));
			Proposition proposition1_Q3 = new Proposition("56%");
			Proposition proposition2_Q3 = new Proposition("33%");
			Proposition proposition3_Q3 = new Proposition("21%");
			Proposition proposition4_Q3 = new Proposition("3%");
			List<Proposition> propositions_Q3 = List.of(proposition1_Q3, proposition2_Q3, proposition3_Q3,
					proposition4_Q3);

			QuestionQCM question3 = new QuestionQCM(DifficulteActivite.MOYEN,
					"En Tunisie quelle est la proportion de femme parmi les étudiants en informatique ?", 2,
					"Seulement 23% de femmes dans le secteur informatique en France. (12% dans le secteur technique de l’IA et chez Volvo la proportion est donc de 33%). Pendant longtemps, programmer était considéré comme une compétence de femmes. (Ada Lovelace a inventé le premier programme informatique, Margaret Hamilton a codé l’appareil qui a atterri sur la lune pour la première fois…) Métier considéré comme une tâche répétitive et inintéressante à l’époque. (moins payé que le secrétariat) Seulement au tout début des années 1990, que ça change en Europe : mise en avant de l’informatique qui sera l’avenir de l’humanité. (Hommes sont poussés dans ces carrières :, en 2002, les DUT informatiques en France comptent 10% de femmes contre 60% en 1980.) Répartition très culturelle (Indonésie, Malaisie, Tunisie : métier considéré comme féminin). Travail qui ne demande pas de force physique, pas salissant, permet de travailler de chez soi, conciliable avec des responsabilités de maman.",
					propositions_Q3, proposition1_Q3);

			plateauGeneral.addActivite(question1);
			plateauGeneral.addActivite(question2);
			plateauGeneral.addActivite(question3);
			plateauRepository.save(plateauGeneral);
			plateauRepository.save(plateauCyber);
			plateauRepository.flush();
			System.out.println("Plateau General ajouté et ses questions");

			// Plateau plateau1 = plateauRepository.findByNom("Général");
			// Plateau plateau2 = plateauRepository.findByNom("Cyber");
			// List<Plateau> listePlateaux = List.of(plateau1, plateau2);

			// MaitreDuJeu maitre = maitreDuJeuRepository.findByNom("admin")
			// .get();
			// Partie partie = playITService.creerPartie("Stage seconde", maitre,
			// listePlateaux);
			// playITService.choisirPlateau(partie, plateau1);
			// ActiviteEnCours activiteEnCours = playITService.lancerActivite(partie);
			// activiteEnCours = playITService.lancerActivite(partie);
		};

	}

}