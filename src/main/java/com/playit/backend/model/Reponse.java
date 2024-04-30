package com.playit.backend.model;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDateTime;
import java.time.Duration;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Table;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(name="reponseunique", columnNames={"equipe", "activiteEnCours"})
        }
        )
public class Reponse {
    @Id
	@GeneratedValue(strategy = GenerationType.TABLE)
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
    
    public Reponse(){
    }
    
    public void calculerScoreEquipe() {
        Question question = ((Question) activiteEnCours.getActivite());
        Proposition bonneProposition = new Proposition();
        if(question instanceof QuestionQCM){
            bonneProposition = ((QuestionQCM) question).getBonneProposition();
        } else if(question instanceof QuestionVraiFaux) {
            bonneProposition = ((QuestionVraiFaux) question).getBonneProposition();
        }

        if(bonneProposition.equals(proposition)) {
            int scoreMax = question.getScore();
            scoreEquipe = scoreMax / 2;
                        
            LocalDateTime debutQuestion = activiteEnCours.getDate();
            Duration dureeQuestion = question.getTemps();
            LocalDateTime finQuestion = debutQuestion.plus(dureeQuestion);
        
            // Normalisation du temps restant
            Duration tempsRestant = Duration.between(finQuestion, dateSoumission);
            long coefficient = tempsRestant.dividedBy(dureeQuestion);

            scoreEquipe += coefficient * (scoreMax / 2);
        } else {
            scoreEquipe = 0;
        }

        equipe.ajouterScore(scoreEquipe);
    }
    public LocalDateTime getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDateTime dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public Proposition getProposition() {
        return proposition;
    }

    public void setProposition(Proposition proposition) {
        this.proposition = proposition;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public int getScoreEquipe() {
        return scoreEquipe;
    }

    public void setScoreEquipe(int scoreEquipe) {
        this.scoreEquipe = scoreEquipe;
    }

    public Long getId() {
        return id;
    }
    
    public ActiviteEnCours getActiviteEnCours() {
        return activiteEnCours;
    }

    public void setActiviteEnCours(ActiviteEnCours activiteEnCours) {
        this.activiteEnCours = activiteEnCours;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Reponse other = (Reponse) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (dateSoumission == null) {
            if (other.dateSoumission != null)
                return false;
        } else if (!dateSoumission.equals(other.dateSoumission))
            return false;
        if (scoreEquipe != other.scoreEquipe)
            return false;
        if (proposition == null) {
            if (other.proposition != null)
                return false;
        } else if (!proposition.equals(other.proposition))
            return false;
        if (equipe == null) {
            if (other.equipe != null)
                return false;
        } else if (!equipe.equals(other.equipe))
            return false;
        if (activiteEnCours == null) {
            if (other.activiteEnCours != null)
                return false;
        } else if (!activiteEnCours.equals(other.activiteEnCours))
            return false;
        return true;
    }
    
    
}