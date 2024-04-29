package com.playit.backend.model;

import javax.persistence.*;

@Entity
@Table
public class Domaine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
}