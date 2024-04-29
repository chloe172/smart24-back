package com.playit.backend.model;

import javax.persistence.*;

@Entity
@Table
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class Activite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int difficulte;
}