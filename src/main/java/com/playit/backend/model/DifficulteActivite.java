package com.playit.backend.model;

public enum DifficulteActivite {
    FACILE(100),
    MOYEN(200),
    DIFFICILE(300),
    EXPERT(400);

    private int points;
    
    private DifficulteActivite(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}