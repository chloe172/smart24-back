package com.playit.backend.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "reponseunique", columnNames = { "equipe", "activiteEnCours" }) })
public class Reponse {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime dateSoumission = LocalDateTime.now();
	private int scoreEquipe;

	@ManyToOne
	private Proposition proposition;

	@ManyToOne
	private Equipe equipe;

	@ManyToOne
	private ActiviteEnCours activiteEnCours;

	public Reponse() {
	}

	public void calculerScoreEquipe() {
		Question question = ((Question) this.activiteEnCours.getActivite());
		Proposition bonneProposition = new Proposition();
		if (question instanceof QuestionQCM) {
			bonneProposition = ((QuestionQCM) question).getBonneProposition();
		} else if (question instanceof QuestionVraiFaux) {
			bonneProposition = ((QuestionVraiFaux) question).getBonneProposition();
		}

		if (bonneProposition.equals(this.proposition)) {
			int scoreMax = question.getScore();
			this.scoreEquipe = scoreMax / 2;

			LocalDateTime debutQuestion = this.activiteEnCours.getDate();
			Duration dureeQuestion = question.getTemps();
			LocalDateTime finQuestion = debutQuestion.plus(dureeQuestion);

			// Normalisation du temps restant
			Duration tempsRestant = Duration.between(finQuestion, this.dateSoumission);
			long coefficient = tempsRestant.dividedBy(dureeQuestion);

			this.scoreEquipe += coefficient * (scoreMax / 2);
		} else {
			this.scoreEquipe = 0;
		}

		this.equipe.ajouterScore(this.scoreEquipe);
	}

	public LocalDateTime getDateSoumission() {
		return this.dateSoumission;
	}

	public void setDateSoumission(LocalDateTime dateSoumission) {
		this.dateSoumission = dateSoumission;
	}

	public Proposition getProposition() {
		return this.proposition;
	}

	public void setProposition(Proposition proposition) {
		this.proposition = proposition;
	}

	public Equipe getEquipe() {
		return this.equipe;
	}

	public void setEquipe(Equipe equipe) {
		this.equipe = equipe;
	}

	public int getScoreEquipe() {
		return this.scoreEquipe;
	}

	public void setScoreEquipe(int scoreEquipe) {
		this.scoreEquipe = scoreEquipe;
	}

	public Long getId() {
		return this.id;
	}

	public ActiviteEnCours getActiviteEnCours() {
		return this.activiteEnCours;
	}

	public void setActiviteEnCours(ActiviteEnCours activiteEnCours) {
		this.activiteEnCours = activiteEnCours;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (this.getClass() != obj.getClass())) {
			return false;
		}
		Reponse other = (Reponse) obj;
		if (!Objects.equals(this.id, other.id) || !Objects.equals(this.dateSoumission, other.dateSoumission)
			|| (this.scoreEquipe != other.scoreEquipe) || !Objects.equals(this.proposition, other.proposition)) {
			return false;
		}
		if (!Objects.equals(this.equipe, other.equipe)
			|| !Objects.equals(this.activiteEnCours, other.activiteEnCours)) {
			return false;
		}
		return true;
	}

}