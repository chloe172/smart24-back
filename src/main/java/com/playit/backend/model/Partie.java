package com.playit.backend.model;

import javax.persistence.*;

@Entity
@Table
public class Partie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int numero;
}