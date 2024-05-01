package com.playit.backend;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.playit.backend.model.ActiviteEnCours;
import com.playit.backend.model.MaitreDuJeu;
import com.playit.backend.model.Partie;
import com.playit.backend.model.Plateau;
import com.playit.backend.repository.MaitreDuJeuRepository;
import com.playit.backend.repository.PlateauRepository;
import com.playit.backend.repository.QuestionRepository;
import com.playit.backend.service.PlayITService;

@SpringBootApplication
@RestController
public class PlayItBackendApplication {
	@Autowired
	QuestionRepository questionRepository;

	@Autowired
	PlateauRepository plateauRepository;
	@Autowired
	MaitreDuJeuRepository maitreDuJeuRepository;
	@Autowired
	PlayITService playITService;

	public static void main(String[] args) {
		SpringApplication.run(PlayItBackendApplication.class, args);
	}

	@GetMapping("/")
	public String index() {
		Plateau plateauGene = this.plateauRepository.findByNom("Général");
		Plateau plateauCyber = this.plateauRepository.findByNom("Cyber");
		List<Plateau> listePlateaux = new ArrayList<Plateau>();
		listePlateaux.add(plateauGene);
		listePlateaux.add(plateauCyber);

		MaitreDuJeu maitre = this.maitreDuJeuRepository.findByNom("admin@volvo.fr")
				.get();
		Partie partie = this.playITService.creerPartie("Stage seconde", maitre, listePlateaux);
		StringBuilder sb = new StringBuilder();
		sb.append("Partie créée : ");
		sb.append(partie.getNom());
		sb.append(" avec ");
		sb.append(partie.getPlateaux().size());
		sb.append(" plateaux");
		sb.append("<br>");
		sb.append("Code PIN : ");
		sb.append(partie.getCodePin());
		return sb.toString();
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

	@GetMapping("/lancerActivite")
	public ActiviteEnCours lancerActivite(@RequestParam(value = "partie") Partie partie) {
		return this.playITService.lancerActivite(partie);
	}
}
