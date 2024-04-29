package com.playit.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table
public class Plateau {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nom;

	@OneToMany(mappedBy = "plateau")
	private List<Activite> listeActivites;

	@OneToOne
	private Activite activiteCourante;

	public Plateau() {
	}

	public Long getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}
	
	public void setNom(String nom) {
		this.nom = nom;
	}
	
	public List<Activite> getListeActivites() {
		return listeActivites;
	}

	public void addActivite(Activite activite) {
		activite.setPlateau(this);
		this.listeActivites.add(activite);
	}
	
	public void setListeActivites(List<Activite> listeActivites) {
		this.listeActivites = listeActivites;
	}
	
	public Activite getActiviteCourante() {
		return activiteCourante;
	}

	public void setActiviteCourante(Activite activiteCourante) {
		this.activiteCourante = activiteCourante;
	}


}