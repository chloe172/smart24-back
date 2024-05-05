package com.playit.backend.metier.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class PlateauEnCours {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Partie partie;

    @ManyToOne
    private Plateau plateau;

    private int dernierIndice = 0;

    public PlateauEnCours() {
    }

    public PlateauEnCours(Plateau plateau, Partie partie) {
        this.plateau = plateau;
        this.partie = partie;
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public void setPlateau(Plateau plateau) {
        this.plateau = plateau;
    }

    public Activite getProchaineActivite() {
        if (dernierIndice >= plateau.getListeActivites().size()) {
            return null;
        }
        Activite activite = plateau.getListeActivites().get(dernierIndice);
        dernierIndice++;
        return activite;
    }

    public int getNombreActivites() {
        return plateau.getListeActivites().size();
    }

    public int getNombreActivitesTerminees() {
        return dernierIndice;
    }

    public boolean estTermine() {
        return dernierIndice >= plateau.getListeActivites().size();
    }

}
