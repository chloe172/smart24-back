package com.playit.backend.model;

import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table
public class Plateau {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String nom;

	@OneToMany(mappedBy = "plateau", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@OrderBy("numeroActivite")
	private List<Activite> listeActivites = new ArrayList<>();

	public Plateau() {
	}

	public Plateau(String nom) {
		this.nom = nom;
	}

	public Long getId() {
		return this.id;
	}

	public String getNom() {
		return this.nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public List<Activite> getListeActivites() {
		return this.listeActivites;
	}

	public void addActivite(Activite activite) {
		activite.setPlateau(this);
		this.listeActivites.add(activite);
	}

	public void setListeActivites(List<Activite> listeActivites) {
		this.listeActivites = listeActivites;
	}

	@Override
	public String toString() {
		return "Plateau {" + " id='" + getId() + "'" + ", nom='" + getNom() + "'" + ", listeActivites='"
				+ getListeActivites()
				+ "'" + "}";
	}

}