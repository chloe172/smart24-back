package com.playit.backend.metier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ActiviteEnCours {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(columnDefinition = "TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime date = LocalDateTime.now();

	@ManyToOne
	private Partie partie;

	@ManyToOne
	private Activite activite;

	@OneToMany(mappedBy = "activiteEnCours", fetch = FetchType.EAGER)
	@OrderBy("dateSoumission")
	private List<Reponse> listeReponses = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public Partie getPartie() {
		return partie;
	}

	public void setPartie(Partie partie) {
		this.partie = partie;
	}

	public Activite getActivite() {
		return activite;
	}

	public void setActivite(Activite activite) {
		this.activite = activite;
	}

	public List<Reponse> getListeReponses() {
		return listeReponses;
	}

	public void setListeReponses(List<Reponse> listeReponses) {
		this.listeReponses = listeReponses;
	}

	public void addReponse(Reponse reponse) {
		reponse.setActiviteEnCours(this);
		this.listeReponses.add(reponse);
	}

}
