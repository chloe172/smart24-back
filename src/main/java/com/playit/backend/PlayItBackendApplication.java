package com.playit.backend;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.playit.backend.metier.model.ActiviteEnCours;
import com.playit.backend.metier.model.Equipe;
import com.playit.backend.metier.model.EtatPartie;
import com.playit.backend.metier.model.MaitreDuJeu;
import com.playit.backend.metier.model.Partie;
import com.playit.backend.metier.model.Plateau;
import com.playit.backend.metier.service.PlayITService;
import com.playit.backend.repository.EquipeRepository;
import com.playit.backend.repository.MaitreDuJeuRepository;
import com.playit.backend.repository.PartieRepository;
import com.playit.backend.repository.PlateauRepository;

@SpringBootApplication
@RestController
public class PlayItBackendApplication {
	@Autowired
	PlateauRepository plateauRepository;
	@Autowired
	MaitreDuJeuRepository maitreDuJeuRepository;
	@Autowired
	PlayITService playITService;
	@Autowired
	PartieRepository partieRepository;
	@Autowired
	EquipeRepository equipeRepository;

	public static void main(String[] args) {
		SpringApplication.run(PlayItBackendApplication.class, args);
	}

	@GetMapping("/")
	public String index() {

		MaitreDuJeu maitre = this.maitreDuJeuRepository.findAll()
				.get(0);
		List<Plateau> listePlateaux = this.plateauRepository.findAll();
		Partie partie = this.playITService.creerPartie("Stage seconde", maitre, listePlateaux);
		StringBuilder sb = new StringBuilder();
		sb.append("Partie créée : ");
		sb.append(partie.getNom());
		sb.append(" avec ");
		sb.append(partie.getPlateauxEnCours()
				.size());
		sb.append(" plateaux");
		sb.append("<br>");
		sb.append("Code PIN : ");
		sb.append(partie.getCodePin());

		Equipe e1 = this.playITService.inscrireEquipe("Equipe 1", partie);
		Equipe e2 = this.playITService.inscrireEquipe("Equipe 2", partie);

		partie.setEtat(EtatPartie.EN_PAUSE);
		this.partieRepository.saveAndFlush(partie);

		e1.setEstConnecte(false);
		e2.setEstConnecte(false);
		this.equipeRepository.saveAndFlush(e1);
		this.equipeRepository.saveAndFlush(e2);

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
