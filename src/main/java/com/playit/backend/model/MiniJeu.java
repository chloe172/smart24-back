package com.playit.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table
public class MiniJeu extends Activite {
	private String intitule;
}