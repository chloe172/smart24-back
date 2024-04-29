package com.playit.backend.model;

import javax.persistence.*;
import java.util.List; 

@Entity
@Table
public class Question extends Activite {
    private String typeQuestion;
    private String intitule;
    private List<String> reponses;
    private String bonneReponse;
    private String explication;

}