package com.playit.backend.model;

import javax.persistence.*;

@Entity
@Table
public class MaitreDuJeu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String motDePasse;
}