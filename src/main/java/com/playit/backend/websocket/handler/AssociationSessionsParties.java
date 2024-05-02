package com.playit.backend.websocket.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.web.socket.WebSocketSession;

import com.playit.backend.metier.model.Partie;

public class AssociationSessionsParties {

	private AssociationSessionsParties() {
	}

	private static HashMap<Long, List<WebSocketSession>> equipesParPartie = new HashMap<>();

	private static HashMap<Long, WebSocketSession> maitreDuJeuParPartie = new HashMap<>();

	public static void ajouterSessionEquipeAPartie(WebSocketSession equipe, Partie partie) {
		Long idPartie = partie.getId();
		List<WebSocketSession> equipes = equipesParPartie.computeIfAbsent(idPartie, k -> new ArrayList<>());
		equipes.add(equipe);
	}

	public static void associerSessionMaitreDuJeuAPartie(WebSocketSession maitreDuJeu, Partie partie) {
		Long idPartie = partie.getId();
		maitreDuJeuParPartie.put(idPartie, maitreDuJeu);
	}

	public static List<WebSocketSession> getEquipesParPartie(Partie partie) {
		return equipesParPartie.get(partie.getId());
	}

	public static WebSocketSession getMaitreDuJeuPartie(Partie partie) {
		return maitreDuJeuParPartie.get(partie.getId());
	}

	public static void retirerSession(WebSocketSession session) {
		for (Entry<Long, List<WebSocketSession>> entry : equipesParPartie.entrySet()) {
			List<WebSocketSession> equipes = entry.getValue();
			if (equipes.contains(session)) {
				equipes.remove(session);
			}
		}
		maitreDuJeuParPartie.values()
		                    .remove(session);
	}

	public static void enleverPartie(Partie partie) {
		Long idPartie = partie.getId();
		equipesParPartie.remove(idPartie);
		maitreDuJeuParPartie.remove(idPartie);
	}

}