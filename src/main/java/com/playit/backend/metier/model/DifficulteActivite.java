package com.playit.backend.metier.model;

public enum DifficulteActivite {
	FACILE(100), MOYEN(200), DIFFICILE(300), EXPERT(400);

	private int points;

	private DifficulteActivite(int points) {
		this.points = points;
	}

	public int getPoints() {
		return points;
	}

	public static DifficulteActivite getDifficulteFromString(String difficulte) {
		switch (difficulte) {
			case "Facile":
				return DifficulteActivite.FACILE;
			case "Moyen":
				return DifficulteActivite.MOYEN;
			case "Difficile":
				return DifficulteActivite.DIFFICILE;
			case "Expert":
				return DifficulteActivite.EXPERT;
			default:
				throw new IllegalArgumentException("Difficulté non prise en compte");

		}
	}
}